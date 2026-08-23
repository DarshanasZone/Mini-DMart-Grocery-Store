/**
 * Mini-DMart Customer Orders Script
 */
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('customerOrdersList')) {
    loadCustomerOrders();
  }
  if (document.getElementById('customerOrderDetailView')) {
    loadCustomerOrderDetail();
  }
});

async function loadCustomerOrders() {
  const container = document.getElementById('customerOrdersList');
  if (!container) return;

  const user = api.getUser();
  if (!user) {
    window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname)}`;
    return;
  }

  container.innerHTML = `<p style="text-align:center; padding:40px; color:var(--gray-500);">Loading your order history...</p>`;

  try {
    const orders = await api.get('/customer/orders');
    if (!orders || orders.length === 0) {
      container.innerHTML = `
        <div style="text-align:center; padding:60px 20px; background:var(--white); border-radius:var(--radius-md); border:1px solid var(--gray-200);">
          <p style="font-size:3rem; margin-bottom:12px;">📦</p>
          <h3 style="color:var(--dark); margin-bottom:8px;">No Orders Yet</h3>
          <p style="color:var(--gray-500); margin-bottom:20px;">You have not placed any grocery orders yet.</p>
          <a href="/products.html" class="btn btn-primary">Start Shopping</a>
        </div>
      `;
      return;
    }

    container.innerHTML = `
      <div class="table-responsive">
        <table class="table">
          <thead>
            <tr>
              <th>Order ID</th>
              <th>Placed On</th>
              <th>Delivery Mode</th>
              <th>Items</th>
              <th>Total Amount</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            ${orders.map(o => {
              const formattedDate = o.createdAt ? new Date(o.createdAt).toLocaleDateString('en-IN', {
                year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
              }) : 'N/A';
              const itemsCount = o.items ? o.items.reduce((s, i) => s + (i.quantity || 1), 0) : 0;
              return `
                <tr>
                  <td><strong>#${o.id}</strong></td>
                  <td>${formattedDate}</td>
                  <td><span class="badge badge-secondary">${escapeHtml(o.deliveryType || 'HOME_DELIVERY')}</span></td>
                  <td>${itemsCount} items</td>
                  <td style="font-weight:700; color:var(--primary);">${formatCurrency(o.totalAmount)}</td>
                  <td><span class="badge order-status-${o.status}">${escapeHtml(o.status)}</span></td>
                  <td>
                    <a href="/order-details.html?id=${o.id}" class="btn btn-outline btn-sm">View Details</a>
                  </td>
                </tr>
              `;
            }).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<p style="color:var(--danger); text-align:center; padding:40px;">Failed to load order history: ${escapeHtml(err.message)}</p>`;
  }
}

async function loadCustomerOrderDetail() {
  const container = document.getElementById('customerOrderDetailView');
  const params = new URLSearchParams(window.location.search);
  const orderId = params.get('id');
  if (!container || !orderId) return;

  const user = api.getUser();
  if (!user) {
    window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname)}`;
    return;
  }

  container.innerHTML = `<p style="text-align:center; padding:40px; color:var(--gray-500);">Loading order details...</p>`;

  try {
    const order = await api.get(`/customer/orders/${orderId}`);
    const formattedDate = order.createdAt ? new Date(order.createdAt).toLocaleString('en-IN') : 'N/A';

    container.innerHTML = `
      <div style="background:var(--white); border-radius:var(--radius-lg); border:1px solid var(--gray-200); padding:30px; box-shadow:var(--shadow-sm);">
        <!-- Order Header -->
        <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:24px; border-bottom:1px solid var(--gray-200); padding-bottom:16px;">
          <div>
            <div style="display:flex; align-items:center; gap:12px; margin-bottom:6px;">
              <h1 style="font-size:1.6rem; font-weight:800; color:var(--dark);">Order #${order.id}</h1>
              <span class="badge order-status-${order.status}" style="font-size:0.85rem;">${escapeHtml(order.status)}</span>
            </div>
            <p style="color:var(--gray-500); font-size:0.9rem;">Placed on ${formattedDate}</p>
          </div>
          <div>
            ${order.canCancel ? `
              <button class="btn btn-danger btn-sm" onclick="cancelCustomerOrder(${order.id})">❌ Cancel Order</button>
            ` : ''}
            <a href="/orders.html" class="btn btn-outline btn-sm">⬅️ All Orders</a>
          </div>
        </div>

        <!-- Order Information Cards -->
        <div style="display:grid; grid-template-columns:1fr 1fr; gap:20px; margin-bottom:28px;">
          <div style="background:var(--gray-100); padding:16px; border-radius:var(--radius-sm);">
            <h4 style="font-size:0.85rem; color:var(--gray-700); margin-bottom:6px;">Fulfillment Mode:</h4>
            <p style="font-weight:700;">${order.deliveryType === 'STORE_PICKUP' ? '🏬 Store Pickup' : '🚚 Home Delivery'}</p>
            <p style="font-size:0.85rem; color:var(--gray-700); margin-top:4px;">
              ${order.deliveryType === 'STORE_PICKUP' ? `Pickup Time: ${order.pickupDate || 'Anytime today'}` : `Address: ${escapeHtml(order.deliveryAddress || 'Standard address')}`}
            </p>
          </div>
          <div style="background:var(--gray-100); padding:16px; border-radius:var(--radius-sm);">
            <h4 style="font-size:0.85rem; color:var(--gray-700); margin-bottom:6px;">Order Value:</h4>
            <p style="font-size:1.3rem; font-weight:800; color:var(--primary);">${formatCurrency(order.totalAmount)}</p>
            <p style="font-size:0.85rem; color:var(--gray-700);">Payment: Simulated at Checkout</p>
          </div>
        </div>

        <!-- Ordered Items Table -->
        <h3 style="font-size:1.2rem; font-weight:700; margin-bottom:14px;">Items in this Order</h3>
        <div class="table-responsive" style="margin-bottom:24px;">
          <table class="table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Unit Price</th>
                <th>Qty</th>
                <th>Subtotal</th>
                ${order.canReturnOrExchange ? '<th>Post-Purchase Actions</th>' : ''}
              </tr>
            </thead>
            <tbody>
              ${(order.items || []).map(item => {
                const defaultImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=600&auto=format&fit=crop&q=80';
                return `
                  <tr>
                    <td>
                      <div style="display:flex; align-items:center; gap:10px;">
                        <img src="${escapeHtml(item.productImageUrl || defaultImg)}" style="width:44px; height:44px; object-fit:cover; border-radius:var(--radius-sm);" onerror="this.src='${defaultImg}'" />
                        <span style="font-weight:700;">${escapeHtml(item.productName)}</span>
                      </div>
                    </td>
                    <td>${formatCurrency(item.price)}</td>
                    <td style="font-weight:700;">${item.quantity}</td>
                    <td style="font-weight:700; color:var(--primary);">${formatCurrency(item.subtotal)}</td>
                    ${order.canReturnOrExchange ? `
                      <td>
                        <div style="display:flex; gap:6px;">
                          <a href="/returns.html?orderId=${order.id}" class="btn btn-outline btn-sm">Return</a>
                          <a href="/exchanges.html?orderId=${order.id}&oldProductId=${item.productId}" class="btn btn-outline btn-sm">Exchange</a>
                        </div>
                      </td>
                    ` : ''}
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        </div>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<p style="color:var(--danger); text-align:center; padding:40px;">Failed to load order details: ${escapeHtml(err.message)}</p>`;
  }
}

async function cancelCustomerOrder(orderId) {
  if (!confirm('Are you sure you want to cancel this order? The reserved inventory will be restored.')) return;

  try {
    await api.patch(`/customer/orders/${orderId}/cancel`);
    api.showToast('Order cancelled successfully.', 'success');
    loadCustomerOrderDetail();
  } catch (err) {
    api.showToast(err.message || 'Failed to cancel order', 'error');
  }
}
