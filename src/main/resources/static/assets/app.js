const state = {
    token: localStorage.getItem('taskManagerToken'),
    currentUserEmail: localStorage.getItem('taskManagerEmail'),
};

const selectors = {
    loginPanel: document.getElementById('login-panel'),
    loginForm: document.getElementById('login-form'),
    appPanel: document.getElementById('app-panel'),
    message: document.getElementById('message'),
    authStatus: document.getElementById('auth-status'),
    logoutBtn: document.getElementById('logout-btn'),
    refreshBtn: document.getElementById('refresh-btn'),
    createForm: document.getElementById('create-form'),
    updateForm: document.getElementById('update-form'),
    updateId: document.getElementById('update-id'),
    updateEmail: document.getElementById('update-email'),
    updateFirstName: document.getElementById('update-firstName'),
    updateLastName: document.getElementById('update-lastName'),
    updatePassword: document.getElementById('update-password'),
    resetUpdate: document.getElementById('reset-update'),
    usersTableBody: document.querySelector('#users-table tbody'),
};

document.addEventListener('DOMContentLoaded', () => {
    toggleAppView(Boolean(state.token));
    wireEventHandlers();
    if (state.token) {
        loadUsers();
    }
});

function wireEventHandlers() {
    selectors.loginForm.addEventListener('submit', handleLogin);
    selectors.logoutBtn.addEventListener('click', handleLogout);
    selectors.refreshBtn.addEventListener('click', loadUsers);
    selectors.createForm.addEventListener('submit', handleCreateUser);
    selectors.updateForm.addEventListener('submit', handleUpdateUser);
    selectors.resetUpdate.addEventListener('click', resetUpdateForm);
}

async function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(selectors.loginForm);
    const payload = Object.fromEntries(formData.entries());

    try {
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            throw new Error('Неверный email или пароль');
        }

        const token = await response.text();
        state.token = token;
        state.currentUserEmail = payload.username;
        localStorage.setItem('taskManagerToken', token);
        localStorage.setItem('taskManagerEmail', payload.username);
        selectors.loginForm.reset();
        toggleAppView(true);
        showMessage('Аутентификация прошла успешно', 'success');
        loadUsers();
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

function handleLogout() {
    state.token = null;
    state.currentUserEmail = null;
    localStorage.removeItem('taskManagerToken');
    localStorage.removeItem('taskManagerEmail');
    resetUpdateForm();
    selectors.loginForm.reset();
    toggleAppView(false);
    showMessage('Вы вышли из системы', 'success');
}

async function handleCreateUser(event) {
    event.preventDefault();
    const formData = new FormData(selectors.createForm);
    const payload = Object.fromEntries(formData.entries());

    try {
        await apiFetch('/api/users', {
            method: 'POST',
            body: payload,
        });
        selectors.createForm.reset();
        showMessage('Пользователь создан', 'success');
        loadUsers();
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

async function handleUpdateUser(event) {
    event.preventDefault();
    const id = selectors.updateId.value;
    if (!id) {
        showMessage('Выберите пользователя для обновления', 'error');
        return;
    }

    const formData = new FormData(selectors.updateForm);
    const entries = Array.from(formData.entries());
    const payload = entries.reduce((acc, [key, value]) => {
        if (key === 'id') {
            return acc;
        }
        const trimmed = value.trim();
        if (trimmed !== '') {
            acc[key] = trimmed;
        }
        return acc;
    }, {});

    if (Object.keys(payload).length === 0) {
        showMessage('Нет данных для обновления', 'error');
        return;
    }

    try {
        await apiFetch(`/api/users/${id}`, {
            method: 'PUT',
            body: payload,
        });
        showMessage('Данные пользователя обновлены', 'success');
        resetUpdateForm();
        loadUsers();
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

async function loadUsers() {
    try {
        const users = await apiFetch('/api/users');
        renderUsers(users);
        showMessage('Список пользователей обновлён', 'success');
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

function renderUsers(users) {
    selectors.usersTableBody.innerHTML = '';
    if (!Array.isArray(users) || users.length === 0) {
        const row = document.createElement('tr');
        const cell = document.createElement('td');
        cell.colSpan = 6;
        cell.textContent = 'Пользователи отсутствуют';
        cell.classList.add('empty-cell');
        row.appendChild(cell);
        selectors.usersTableBody.appendChild(row);
        return;
    }

    users.forEach((user) => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${user.id}</td>
            <td>${user.email}</td>
            <td>${user.firstName ?? ''}</td>
            <td>${user.lastName ?? ''}</td>
            <td>${user.createdAt ?? ''}</td>
            <td class="data-table__actions">
                <button class="button button--secondary" data-action="edit">Редактировать</button>
                <button class="button" data-action="delete">Удалить</button>
            </td>
        `;
        row.querySelector('[data-action="edit"]').addEventListener('click', () => populateUpdateForm(user));
        row.querySelector('[data-action="delete"]').addEventListener('click', () => deleteUser(user));
        selectors.usersTableBody.appendChild(row);
    });
}

function populateUpdateForm(user) {
    selectors.updateId.value = user.id;
    selectors.updateEmail.value = user.email ?? '';
    selectors.updateFirstName.value = user.firstName ?? '';
    selectors.updateLastName.value = user.lastName ?? '';
    selectors.updatePassword.value = '';
    selectors.updateEmail.focus();
}

async function deleteUser(user) {
    if (!confirm(`Удалить пользователя ${user.email}?`)) {
        return;
    }

    try {
        await apiFetch(`/api/users/${user.id}`, { method: 'DELETE' });
        showMessage('Пользователь удалён', 'success');
        if (selectors.updateId.value === String(user.id)) {
            resetUpdateForm();
        }
        loadUsers();
    } catch (error) {
        showMessage(error.message, 'error');
    }
}

function resetUpdateForm() {
    selectors.updateForm.reset();
    selectors.updateId.value = '';
}

function toggleAppView(isAuthenticated) {
    selectors.loginPanel.classList.toggle('hidden', isAuthenticated);
    selectors.appPanel.classList.toggle('hidden', !isAuthenticated);
    selectors.logoutBtn.classList.toggle('hidden', !isAuthenticated);
    if (isAuthenticated) {
        selectors.authStatus.textContent = `Вы вошли как ${state.currentUserEmail ?? 'пользователь'}`;
    } else {
        selectors.authStatus.textContent = 'Войдите с помощью email и пароля';
    }
}

async function apiFetch(url, options = {}) {
    if (!state.token) {
        throw new Error('Необходима аутентификация');
    }

    const config = {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${state.token}`,
        },
        ...options,
    };

    if (config.body && typeof config.body === 'string') {
        // body already stringified
    } else if (config.body) {
        config.body = JSON.stringify(config.body);
    }

    const response = await fetch(url, config);

    if (response.status === 401) {
        handleLogout();
        throw new Error('Сессия истекла. Войдите снова.');
    }

    if (response.status === 403) {
        throw new Error('Недостаточно прав для выполнения операции');
    }

    if (!response.ok) {
        const message = await readError(response);
        throw new Error(message || 'Ошибка при выполнении запроса');
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
        return response.json();
    }

    return response.text();
}

async function readError(response) {
    try {
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            const data = await response.json();
            return data.message || JSON.stringify(data);
        }
        return await response.text();
    } catch (error) {
        return response.statusText;
    }
}

function showMessage(text, type) {
    if (!text) {
        selectors.message.classList.add('hidden');
        selectors.message.textContent = '';
        selectors.message.classList.remove('message--success', 'message--error');
        return;
    }

    selectors.message.textContent = text;
    selectors.message.classList.remove('hidden');
    selectors.message.classList.toggle('message--success', type === 'success');
    selectors.message.classList.toggle('message--error', type === 'error');
}
