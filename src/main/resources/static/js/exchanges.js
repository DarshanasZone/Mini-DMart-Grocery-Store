/**
 * Mini-DMart Customer Exchanges Script
 */
let storeProducts = [];

document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('exchangesHistoryContainer')) {
    initExchangesPage();
  }
});

async function initExchangesPage() {
  const user = api.getUser();
  if (!user) {
    window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname)}`;
    return;
  }

  // Pre-fill Order ID and Old Product ID from URL
  const params = new URLSearchParams(window.location.search);
  const orderId = params.get('orderId');
  const oldProductId = params.get('oldProductId');

  if (orderId && document.getElementById('exchangeOrderIdInput')) {
    document.getElementById('exchangeOrderIdInput').value = orderId;
  }

  await loadProductsForExchange(oldProductId);
  await loadExchangesHistory();
}

async function loadProductsForExchange(preselectedOldProductId) {
  try {
    storeProducts = await api.get('/customer/products');
    const oldSelect = document.getElementById('exchangeOldProductSelect');
    const newSelect = document.getElementById('exchangeNewProductSelect');

    if (oldSelect && newSelect) {
      const optionsHtml = storeProducts.map(p => `
        <option value="${p.id}">${escapeHtml(p.name)} (${formatCurrency(p.price)})</option>
      `).join('');

      oldSelect.innerHTML = `<option value="">-- Select Item to Exchange --</option>` + optionsHtml;
      newSelect.innerHTML = `<option value="">-- Select Desired Replacement --</option>` + optionsHtml;

      if (preselectedOldProductId) {
        oldSelect.value = preselectedOldProductId;
      }
    }
  } catch (err) {
    console.error('Failed to load products for exchange select:', err);
  }
}

async function loadExchangesHistory() {
  const container = document.getElementById('exchangesHistoryContainer');
  if (!container) return;

  try {
    const exchanges = await api.get('/customer/exchanges');
    if (!exchanges || exchanges.length === 0) {
      container.innerHTML = `
        <div style="text-align:center; padding:40px; color:var(--gray-500);">
          No exchange requests submitted yet.
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Req ID</th>
              <th>Order ID</th>
              <th>Original Item</th>
              <th>Replacement Item</th>
              <th>Reason</th>
              <th>Status</th>
              <th>Requested On</th>
            </tr>
          </thead>
          <tbody>
            ${exchanges.map(e => `
              <tr>
                <td><strong>#${e.id}</strong></td>
                <td><a href="/order-details.html?id=${e.orderId}">#${e.orderId}</a></td>
                <td>${escapeHtml(e.oldProductName)}</td>
                <td><strong>${escapeHtml(e.newProductName)}</strong></td>
                <td>${escapeHtml(e.reason)}</td>
                <td><span class="badge return-status-${e.status}">${escapeHtml(e.status)}</span></td>
                <td>${e.createdAt ? new Date(e.createdAt).toLocaleDateString('en-IN') : 'N/A'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<p style="color:var(--danger); padding:20px;">Failed to load exchange history.</p>`;
  }
}

async function handleExchangeSubmit(e) {
  e.preventDefault();
  const orderId = document.getElementById('exchangeOrderIdInput').value;
  const oldProductId = document.getElementById('exchangeOldProductSelect').value;
  const newProductId = document.getElementById('exchangeNewProductSelect').value;
  const reason = document.getElementById('exchangeReasonInput').value;

  if (oldProductId === newProductId) {
    api.showToast('Please select a different product for replacement.', 'error');
    return;
  }

  try {
    await api.post('/customer/exchanges', {
      orderId: parseInt(orderId),
      oldProductId: parseInt(oldProductId),
      newProductId: parseInt(newProductId),
      reason
    });
    api.showToast('Exchange request submitted for admin review.', 'success');
    document.getElementById('exchangeForm').reset();
    await loadExchangesHistory();
  } catch (err) {
    api.showToast(err.message || 'Failed to submit exchange request', 'error');
  }
}
