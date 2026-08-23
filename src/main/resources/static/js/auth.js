/**
 * Mini-DMart Authentication & Navigation State
 */
document.addEventListener('DOMContentLoaded', () => {
  renderNavbarAuth();
  updateCartBadge();
});

function renderNavbarAuth() {
  const user = api.getUser();
  const authNav = document.getElementById('authNav');
  const adminNav = document.getElementById('adminPortalNav');
  const ordersNav = document.getElementById('customerOrdersNav');
  const returnsNav = document.getElementById('customerReturnsNav');
  const exchangesNav = document.getElementById('customerExchangesNav');

  const isAdmin = user && (user.role === 'ADMIN' || user.role === 'ROLE_ADMIN');

  if (adminNav) {
    adminNav.style.display = isAdmin ? 'inline-flex' : 'none';
  }
  if (ordersNav) {
    ordersNav.style.display = user ? 'inline-flex' : 'none';
  }
  if (returnsNav) {
    returnsNav.style.display = user ? 'inline-flex' : 'none';
  }
  if (exchangesNav) {
    exchangesNav.style.display = user ? 'inline-flex' : 'none';
  }

  if (authNav) {
    if (user) {
      authNav.innerHTML = `
        <div style="display:flex; align-items:center; gap:10px;">
          <span style="font-size:0.85rem; font-weight:600; color:var(--dark);">
            👤 ${escapeHtml(user.name || user.email)}
            <span class="badge ${isAdmin ? 'badge-danger' : 'badge-success'}" style="font-size:0.65rem; margin-left:4px;">
              ${isAdmin ? 'Admin' : 'Customer'}
            </span>
          </span>
          <button class="btn btn-outline btn-sm" onclick="logout()">Logout</button>
        </div>
      `;
    } else {
      authNav.innerHTML = `
        <div style="display:flex; align-items:center; gap:8px;">
          <a href="/login.html" class="btn btn-outline btn-sm">Login</a>
          <a href="/register.html" class="btn btn-primary btn-sm">Register</a>
        </div>
      `;
    }
  }
}

async function updateCartBadge() {
  const badge = document.getElementById('cartBadgeCount');
  if (!badge) return;

  const user = api.getUser();
  if (!user) {
    badge.textContent = '0';
    return;
  }

  try {
    const cart = await api.get('/customer/cart');
    let totalItems = 0;
    if (cart && cart.items) {
      totalItems = cart.items.reduce((sum, item) => sum + (item.quantity || 1), 0);
    }
    badge.textContent = totalItems;
  } catch (e) {
    badge.textContent = '0';
  }
}

function logout() {
  api.clearAuth();
  api.showToast('Logged out successfully.', 'info');
  setTimeout(() => {
    window.location.href = '/index.html';
  }, 500);
}

function requireAuth(role = null) {
  const user = api.getUser();
  if (!user) {
    window.location.href = '/login.html?redirect=' + encodeURIComponent(window.location.pathname);
    return false;
  }

  if (role === 'ADMIN') {
    const isAdmin = user.role === 'ADMIN' || user.role === 'ROLE_ADMIN';
    if (!isAdmin) {
      alert('Access Denied: Admin privileges required.');
      window.location.href = '/index.html';
      return false;
    }
  }
  return true;
}
