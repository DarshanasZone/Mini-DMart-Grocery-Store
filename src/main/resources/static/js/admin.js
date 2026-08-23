/**
 * Mini-DMart Admin Portal Unified Management Script
 */
document.addEventListener('DOMContentLoaded', () => {
  if (window.location.pathname.includes('/admin/')) {
    if (!requireAuth('ADMIN')) return;

    if (document.getElementById('adminDashboardContainer')) initAdminDashboard();
    if (document.getElementById('adminCategoriesTable')) initAdminCategories();
    if (document.getElementById('adminProductsTable')) initAdminProducts();
    if (document.getElementById('adminOrdersTable')) initAdminOrders();
    if (document.getElementById('adminReturnsTable')) initAdminReturns();
    if (document.getElementById('adminExchangesTable')) initAdminExchanges();
    if (document.getElementById('adminAuditLogsTable')) initAdminAuditLogs();
  }
});

// --- DASHBOARD ---
async function initAdminDashboard() {
  try {
    const [orders, products, returns, exchanges] = await Promise.all([
      api.get('/admin/orders'),
      api.get('/admin/products'),
      api.get('/admin/returns'),
      api.get('/admin/exchanges')
    ]);

    const totalRevenue = (orders || []).reduce((sum, o) => {
      return o.status !== 'CANCELLED' ? sum + Number(o.totalAmount || 0) : sum;
    }, 0);

    const pendingReturns = (returns || []).filter(r => r.status === 'REQUESTED').length;
    const pendingExchanges = (exchanges || []).filter(e => e.status === 'REQUESTED').length;

    document.getElementById('kpiRevenue').textContent = formatCurrency(totalRevenue);
    document.getElementById('kpiOrders').textContent = (orders || []).length;
    document.getElementById('kpiProducts').textContent = (products || []).length;
    document.getElementById('kpiPendingTasks').textContent = pendingReturns + pendingExchanges;

    // Render Recent Orders on Dashboard
    const recentOrdersContainer = document.getElementById('dashboardRecentOrders');
    if (recentOrdersContainer) {
      const recent = (orders || []).slice(0, 5);
      recentOrdersContainer.innerHTML = recent.map(o => `
        <tr>
          <td><strong>#${o.id}</strong></td>
          <td>${escapeHtml(o.userName || o.userEmail)}</td>
          <td>${formatCurrency(o.totalAmount)}</td>
          <td><span class="badge order-status-${o.status}">${escapeHtml(o.status)}</span></td>
          <td>${o.createdAt ? new Date(o.createdAt).toLocaleDateString('en-IN') : 'N/A'}</td>
        </tr>
      `).join('');
    }
  } catch (err) {
    console.error('Failed to load admin dashboard:', err);
  }
}

// --- CATEGORIES ---
async function initAdminCategories() {
  await loadAdminCategoriesList();
}

async function loadAdminCategoriesList() {
  const tableBody = document.getElementById('adminCategoriesTable');
  if (!tableBody) return;

  try {
    const categories = await api.get('/admin/categories');
    tableBody.innerHTML = categories.map(c => `
      <tr>
        <td><strong>#${c.id}</strong></td>
        <td><strong>${escapeHtml(c.name)}</strong></td>
        <td>${escapeHtml(c.description || '-')}</td>
        <td><span class="badge ${c.active ? 'badge-success' : 'badge-danger'}">${c.active ? 'Active' : 'Inactive'}</span></td>
        <td>
          <button class="btn btn-outline btn-sm" onclick="editCategoryModal(${c.id}, '${escapeHtml(c.name)}', '${escapeHtml(c.description || '')}')">✏️ Edit</button>
          <button class="btn btn-danger btn-sm" onclick="deleteCategory(${c.id})">🗑️</button>
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tableBody.innerHTML = `<tr><td colspan="5" style="color:var(--danger); text-align:center;">Failed to load categories</td></tr>`;
  }
}

async function handleCategorySubmit(e) {
  e.preventDefault();
  const id = document.getElementById('categoryIdInput').value;
  const name = document.getElementById('categoryNameInput').value;
  const description = document.getElementById('categoryDescInput').value;

  const payload = { name, description, active: true };

  try {
    if (id) {
      await api.put(`/admin/categories/${id}`, payload);
      api.showToast('Category updated successfully!', 'success');
    } else {
      await api.post('/admin/categories', payload);
      api.showToast('Category created successfully!', 'success');
    }
    closeModal('categoryModal');
    loadAdminCategoriesList();
  } catch (err) {
    api.showToast(err.message || 'Category action failed', 'error');
  }
}

function openCategoryModal() {
  document.getElementById('categoryForm').reset();
  document.getElementById('categoryIdInput').value = '';
  document.getElementById('categoryModalTitle').textContent = 'Add New Department Category';
  openModal('categoryModal');
}

function editCategoryModal(id, name, desc) {
  document.getElementById('categoryIdInput').value = id;
  document.getElementById('categoryNameInput').value = name;
  document.getElementById('categoryDescInput').value = desc;
  document.getElementById('categoryModalTitle').textContent = 'Edit Department Category';
  openModal('categoryModal');
}

async function deleteCategory(id) {
  if (!confirm('Are you sure you want to delete this category?')) return;
  try {
    await api.delete(`/admin/categories/${id}`);
    api.showToast('Category deleted', 'info');
    loadAdminCategoriesList();
  } catch (err) {
    api.showToast(err.message || 'Failed to delete category', 'error');
  }
}

// --- PRODUCTS ---
let adminCategoryOptions = [];

async function initAdminProducts() {
  adminCategoryOptions = await api.get('/admin/categories');
  populateCategorySelect();
  await loadAdminProductsList();
}

function populateCategorySelect() {
  const select = document.getElementById('productCategorySelect');
  if (!select) return;
  select.innerHTML = adminCategoryOptions.map(c => `
    <option value="${c.id}">${escapeHtml(c.name)}</option>
  `).join('');
}

async function loadAdminProductsList() {
  const tableBody = document.getElementById('adminProductsTable');
  if (!tableBody) return;

  try {
    const products = await api.get('/admin/products');
    tableBody.innerHTML = products.map(p => {
      const defaultImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=600&auto=format&fit=crop&q=80';
      return `
        <tr>
          <td><strong>#${p.id}</strong></td>
          <td>
            <div style="display:flex; align-items:center; gap:10px;">
              <img src="${escapeHtml(p.imageUrl || defaultImg)}" style="width:40px; height:40px; object-fit:cover; border-radius:var(--radius-sm);" onerror="this.src='${defaultImg}'" />
              <div>
                <strong>${escapeHtml(p.name)}</strong>
                <div style="font-size:0.75rem; color:var(--gray-500);">${escapeHtml(p.category?.name || '')}</div>
              </div>
            </div>
          </td>
          <td>${formatCurrency(p.price)}</td>
          <td>
            <span class="badge ${p.stockQuantity > 5 ? 'badge-success' : p.stockQuantity > 0 ? 'badge-warning' : 'badge-danger'}">
              ${p.stockQuantity} units
            </span>
          </td>
          <td>
            <span class="badge ${p.active ? 'badge-success' : 'badge-danger'}">
              ${p.active ? 'Active' : 'Inactive'}
            </span>
          </td>
          <td>
            <button class="btn btn-outline btn-sm" onclick="editProductModal(${p.id})">✏️</button>
            <button class="btn btn-outline btn-sm" onclick="toggleProductStatus(${p.id})">
              ${p.active ? 'Deactivate' : 'Activate'}
            </button>
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tableBody.innerHTML = `<tr><td colspan="6" style="color:var(--danger); text-align:center;">Failed to load products</td></tr>`;
  }
}

async function handleProductSubmit(e) {
  e.preventDefault();
  const id = document.getElementById('productIdInput').value;
  const name = document.getElementById('productNameInput').value;
  const description = document.getElementById('productDescInput').value;
  const price = parseFloat(document.getElementById('productPriceInput').value);
  const stockQuantity = parseInt(document.getElementById('productStockInput').value);
  const categoryId = parseInt(document.getElementById('productCategorySelect').value);
  const imageUrl = document.getElementById('productImageUrlInput').value;

  const payload = {
    name,
    description,
    price,
    stockQuantity,
    imageUrl,
    category: { id: categoryId },
    active: true
  };

  try {
    if (id) {
      await api.put(`/admin/products/${id}`, payload);
      api.showToast('Product updated successfully!', 'success');
    } else {
      await api.post('/admin/products', payload);
      api.showToast('Product created successfully!', 'success');
    }
    closeModal('productModal');
    loadAdminProductsList();
  } catch (err) {
    api.showToast(err.message || 'Product action failed', 'error');
  }
}

async function editProductModal(id) {
  try {
    const p = await api.get(`/products/${id}`);
    document.getElementById('productIdInput').value = p.id;
    document.getElementById('productNameInput').value = p.name;
    document.getElementById('productDescInput').value = p.description || '';
    document.getElementById('productPriceInput').value = p.price;
    document.getElementById('productStockInput').value = p.stockQuantity;
    document.getElementById('productCategorySelect').value = p.category?.id || '';
    document.getElementById('productImageUrlInput').value = p.imageUrl || '';
    document.getElementById('productModalTitle').textContent = 'Edit Product';
    openModal('productModal');
  } catch (err) {
    api.showToast('Failed to load product details for edit', 'error');
  }
}

function openProductModal() {
  document.getElementById('productForm').reset();
  document.getElementById('productIdInput').value = '';
  document.getElementById('productModalTitle').textContent = 'Add New Grocery Product';
  openModal('productModal');
}

async function toggleProductStatus(id) {
  try {
    await api.patch(`/admin/products/${id}/status`);
    api.showToast('Product status updated', 'success');
    loadAdminProductsList();
  } catch (err) {
    api.showToast(err.message || 'Failed to toggle status', 'error');
  }
}

// --- ORDERS ---
async function initAdminOrders() {
  await loadAdminOrdersList();
}

async function loadAdminOrdersList() {
  const tableBody = document.getElementById('adminOrdersTable');
  if (!tableBody) return;

  try {
    const orders = await api.get('/admin/orders');
    tableBody.innerHTML = orders.map(o => {
      const formattedDate = o.createdAt ? new Date(o.createdAt).toLocaleDateString('en-IN', {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
      }) : 'N/A';
      return `
        <tr>
          <td><strong>#${o.id}</strong></td>
          <td>
            <strong>${escapeHtml(o.userName || 'Customer')}</strong>
            <div style="font-size:0.75rem; color:var(--gray-500);">${escapeHtml(o.userEmail)}</div>
          </td>
          <td>${formattedDate}</td>
          <td>${formatCurrency(o.totalAmount)}</td>
          <td><span class="badge order-status-${o.status}">${escapeHtml(o.status)}</span></td>
          <td>
            <select class="form-control" style="font-size:0.8rem; padding:4px 8px; width:auto; display:inline-block;" onchange="updateOrderStatus(${o.id}, this.value)">
              <option value="PLACED" ${o.status === 'PLACED' ? 'selected' : ''}>PLACED</option>
              <option value="CONFIRMED" ${o.status === 'CONFIRMED' ? 'selected' : ''}>CONFIRMED</option>
              <option value="SHIPPED" ${o.status === 'SHIPPED' ? 'selected' : ''}>SHIPPED</option>
              <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
              <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
            </select>
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tableBody.innerHTML = `<tr><td colspan="6" style="color:var(--danger); text-align:center;">Failed to load orders</td></tr>`;
  }
}

async function updateOrderStatus(orderId, newStatus) {
  try {
    await api.patch(`/admin/orders/${orderId}/status`, { status: newStatus });
    api.showToast(`Order #${orderId} status updated to ${newStatus}`, 'success');
    loadAdminOrdersList();
  } catch (err) {
    api.showToast(err.message || 'Failed to update order status', 'error');
  }
}

// --- RETURNS ---
async function initAdminReturns() {
  await loadAdminReturnsList();
}

async function loadAdminReturnsList() {
  const tableBody = document.getElementById('adminReturnsTable');
  if (!tableBody) return;

  try {
    const returns = await api.get('/admin/returns');
    tableBody.innerHTML = returns.map(r => `
      <tr>
        <td><strong>#${r.id}</strong></td>
        <td><a href="/order-details.html?id=${r.orderId}">#${r.orderId}</a></td>
        <td>${escapeHtml(r.userEmail)}</td>
        <td>${escapeHtml(r.reason)}</td>
        <td><span class="badge return-status-${r.status}">${escapeHtml(r.status)}</span></td>
        <td>
          ${r.status === 'REQUESTED' ? `
            <button class="btn btn-primary btn-sm" onclick="approveReturn(${r.id})">✅ Approve & Restock</button>
            <button class="btn btn-danger btn-sm" onclick="rejectReturn(${r.id})">❌ Reject</button>
          ` : `<span style="color:var(--gray-500); font-size:0.85rem;">Processed</span>`}
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tableBody.innerHTML = `<tr><td colspan="6" style="color:var(--danger); text-align:center;">Failed to load returns</td></tr>`;
  }
}

async function approveReturn(id) {
  if (!confirm('Approve return? This will automatically restore item stock to store inventory.')) return;
  try {
    await api.patch(`/admin/returns/${id}/approve`);
    api.showToast('Return approved and items restocked.', 'success');
    loadAdminReturnsList();
  } catch (err) {
    api.showToast(err.message || 'Failed to approve return', 'error');
  }
}

async function rejectReturn(id) {
  if (!confirm('Reject this return request?')) return;
  try {
    await api.patch(`/admin/returns/${id}/reject`);
    api.showToast('Return request rejected.', 'info');
    loadAdminReturnsList();
  } catch (err) {
    api.showToast(err.message || 'Failed to reject return', 'error');
  }
}

// --- EXCHANGES ---
async function initAdminExchanges() {
  await loadAdminExchangesList();
}

async function loadAdminExchangesList() {
  const tableBody = document.getElementById('adminExchangesTable');
  if (!tableBody) return;

  try {
    const exchanges = await api.get('/admin/exchanges');
    tableBody.innerHTML = exchanges.map(e => `
      <tr>
        <td><strong>#${e.id}</strong></td>
        <td><a href="/order-details.html?id=${e.orderId}">#${e.orderId}</a></td>
        <td>${escapeHtml(e.userEmail)}</td>
        <td>${escapeHtml(e.oldProductName)}</td>
        <td><strong>${escapeHtml(e.newProductName)}</strong></td>
        <td>${escapeHtml(e.reason)}</td>
        <td><span class="badge return-status-${e.status}">${escapeHtml(e.status)}</span></td>
        <td>
          ${e.status === 'REQUESTED' ? `
            <button class="btn btn-primary btn-sm" onclick="approveExchange(${e.id})">✅ Approve & Reserve Stock</button>
            <button class="btn btn-danger btn-sm" onclick="rejectExchange(${e.id})">❌ Reject</button>
          ` : `<span style="color:var(--gray-500); font-size:0.85rem;">Processed</span>`}
        </td>
      </tr>
    `).join('');
  } catch (err) {
    tableBody.innerHTML = `<tr><td colspan="8" style="color:var(--danger); text-align:center;">Failed to load exchanges</td></tr>`;
  }
}

async function approveExchange(id) {
  if (!confirm('Approve exchange? This will reserve the replacement item stock and restock the returned item.')) return;
  try {
    await api.patch(`/admin/exchanges/${id}/approve`);
    api.showToast('Exchange approved and inventory adjusted.', 'success');
    loadAdminExchangesList();
  } catch (err) {
    api.showToast(err.message || 'Failed to approve exchange', 'error');
  }
}

async function rejectExchange(id) {
  if (!confirm('Reject this exchange request?')) return;
  try {
    await api.patch(`/admin/exchanges/${id}/reject`);
    api.showToast('Exchange request rejected.', 'info');
    loadAdminExchangesList();
  } catch (err) {
    api.showToast(err.message || 'Failed to reject exchange', 'error');
  }
}

// --- AUDIT LOGS ---
async function initAdminAuditLogs() {
  const tableBody = document.getElementById('adminAuditLogsTable');
  if (!tableBody) return;

  try {
    const logs = await api.get('/admin/audit-logs');
    tableBody.innerHTML = logs.map(l => {
      const formattedDate = l.timestamp ? new Date(l.timestamp).toLocaleString('en-IN') : 'N/A';
      return `
        <tr>
          <td><strong>#${l.id}</strong></td>
          <td>${formattedDate}</td>
          <td>${escapeHtml(l.userEmail)}</td>
          <td><span class="badge badge-info">${escapeHtml(l.action)}</span></td>
          <td>${escapeHtml(l.entityName)} #${l.entityId || ''}</td>
          <td style="font-size:0.85rem; color:var(--gray-700);">${escapeHtml(l.details || '-')}</td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tableBody.innerHTML = `<tr><td colspan="6" style="color:var(--danger); text-align:center;">Failed to load audit logs</td></tr>`;
  }
}

// Modal Utility Helpers
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('open');
}
function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('open');
}
