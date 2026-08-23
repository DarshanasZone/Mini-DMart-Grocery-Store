/**
 * Mini-DMart Shopping Cart & Checkout Script
 */
let currentCart = null;

document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('cartPageContainer')) {
    initCartPage();
  }
});

async function initCartPage() {
  const user = api.getUser();
  if (!user) {
    window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname)}`;
    return;
  }
  await loadCart();
}

async function loadCart() {
  const container = document.getElementById('cartPageContainer');
  if (!container) return;

  try {
    currentCart = await api.get('/customer/cart');
    renderCart(currentCart);
    updateCartBadge();
  } catch (err) {
    container.innerHTML = `
      <div style="text-align:center; padding:60px 20px; background:var(--white); border-radius:var(--radius-md);">
        <p style="color:var(--danger); font-size:1.1rem; margin-bottom:12px;">Failed to load shopping cart.</p>
        <button class="btn btn-primary" onclick="loadCart()">Retry</button>
      </div>
    `;
  }
}

function renderCart(cart) {
  const container = document.getElementById('cartPageContainer');
  if (!container) return;

  const items = cart?.items || [];
  if (items.length === 0) {
    container.innerHTML = `
      <div style="text-align:center; padding:70px 20px; background:var(--white); border-radius:var(--radius-md); border:1px solid var(--gray-200);">
        <p style="font-size:3.5rem; margin-bottom:16px;">🛒</p>
        <h2 style="font-size:1.6rem; color:var(--dark); margin-bottom:8px;">Your Shopping Cart is Empty</h2>
        <p style="color:var(--gray-500); margin-bottom:24px;">Explore our departments and fill your basket with fresh groceries.</p>
        <a href="/products.html" class="btn btn-primary btn-lg">Browse Products</a>
      </div>
    `;
    return;
  }

  let subtotal = 0;
  items.forEach(item => {
    const price = item.product?.price || 0;
    subtotal += price * item.quantity;
  });

  const deliveryFee = subtotal >= 500 ? 0 : 40;
  const grandTotal = subtotal + deliveryFee;

  container.innerHTML = `
    <div style="display:grid; grid-template-columns:2fr 1fr; gap:30px; align-items:start;">
      <!-- Cart Items Table -->
      <div style="background:var(--white); border-radius:var(--radius-md); border:1px solid var(--gray-200); padding:24px; box-shadow:var(--shadow-sm);">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:20px; border-bottom:1px solid var(--gray-200); padding-bottom:12px;">
          <h2 style="font-size:1.3rem; font-weight:800;">Cart Items (${items.length})</h2>
          <button class="btn btn-outline btn-sm" onclick="clearEntireCart()">🗑️ Clear Cart</button>
        </div>

        <div class="table-responsive">
          <table class="table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Price</th>
                <th>Quantity</th>
                <th>Subtotal</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              ${items.map(item => {
                const p = item.product;
                const itemSubtotal = (p.price || 0) * item.quantity;
                const defaultImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=600&auto=format&fit=crop&q=80';
                return `
                  <tr>
                    <td>
                      <div style="display:flex; align-items:center; gap:12px;">
                        <img src="${escapeHtml(p.imageUrl || defaultImg)}" style="width:50px; height:50px; object-fit:cover; border-radius:var(--radius-sm);" onerror="this.src='${defaultImg}'" />
                        <div>
                          <a href="/product-details.html?id=${p.id}" style="font-weight:700; color:var(--dark);">${escapeHtml(p.name)}</a>
                          <div style="font-size:0.75rem; color:var(--gray-500);">${escapeHtml(p.category?.name || '')}</div>
                        </div>
                      </div>
                    </td>
                    <td style="font-weight:600;">${formatCurrency(p.price)}</td>
                    <td>
                      <div style="display:inline-flex; align-items:center; border:1px solid var(--gray-300); border-radius:var(--radius-sm);">
                        <button style="background:none; border:none; padding:4px 10px; cursor:pointer; font-weight:bold;" onclick="updateCartItemQty(${p.id}, ${item.quantity - 1})">-</button>
                        <span style="padding:0 8px; font-weight:700;">${item.quantity}</span>
                        <button style="background:none; border:none; padding:4px 10px; cursor:pointer; font-weight:bold;" onclick="updateCartItemQty(${p.id}, ${item.quantity + 1})">+</button>
                      </div>
                    </td>
                    <td style="font-weight:800; color:var(--primary);">${formatCurrency(itemSubtotal)}</td>
                    <td>
                      <button class="btn btn-outline btn-sm" style="color:var(--danger); border-color:var(--danger-light);" onclick="removeCartItem(${p.id})">❌</button>
                    </td>
                  </tr>
                `;
              }).join('')}
            </tbody>
          </table>
        </div>
      </div>

      <!-- Checkout Summary Card -->
      <div style="background:var(--white); border-radius:var(--radius-md); border:1px solid var(--gray-200); padding:24px; box-shadow:var(--shadow-sm);">
        <h3 style="font-size:1.2rem; font-weight:800; margin-bottom:16px; border-bottom:1px solid var(--gray-200); padding-bottom:10px;">Order Summary</h3>
        
        <div style="display:flex; justify-content:space-between; margin-bottom:10px; font-size:0.95rem;">
          <span style="color:var(--gray-700);">Items Total:</span>
          <span style="font-weight:700;">${formatCurrency(subtotal)}</span>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:12px; font-size:0.95rem;">
          <span style="color:var(--gray-700);">Delivery Charge:</span>
          <span style="font-weight:700; color:${deliveryFee === 0 ? 'var(--primary)' : 'var(--dark)'};">
            ${deliveryFee === 0 ? 'FREE (Orders > ₹500)' : formatCurrency(deliveryFee)}
          </span>
        </div>
        <div style="display:flex; justify-content:space-between; margin-bottom:20px; font-size:1.2rem; border-top:1.5px dashed var(--gray-300); padding-top:12px;">
          <span style="font-weight:800;">Estimated Total:</span>
          <span style="font-weight:800; color:var(--primary);">${formatCurrency(grandTotal)}</span>
        </div>

        <form id="checkoutForm" onsubmit="handlePlaceOrder(event)">
          <div class="form-group">
            <label class="form-label">Delivery Mode *</label>
            <select id="orderDeliveryType" class="form-control" onchange="toggleDeliveryInputs()" required>
              <option value="HOME_DELIVERY">🚚 Home Delivery (Standard)</option>
              <option value="STORE_PICKUP">🏬 Store Pickup (Instant)</option>
            </select>
          </div>

          <div class="form-group" id="addressGroup">
            <label class="form-label">Delivery Address *</label>
            <textarea id="orderAddress" class="form-control" rows="2" placeholder="Enter flat/street, landmark, pincode..." required>Flat 402, Green Valley Apartments, Mumbai - 400001</textarea>
          </div>

          <div class="form-group" id="pickupGroup" style="display:none;">
            <label class="form-label">Select Pickup Slot *</label>
            <input type="datetime-local" id="orderPickupDate" class="form-control" />
          </div>

          <div class="form-group">
            <label class="form-label">Payment Method (Simulated)</label>
            <select id="orderPaymentMethod" class="form-control">
              <option value="CASH_ON_DELIVERY">💵 Cash on Delivery (COD)</option>
              <option value="UPI">📱 UPI / QR Code (Simulated)</option>
              <option value="CARD">💳 Credit / Debit Card (Simulated)</option>
            </select>
          </div>

          <button type="submit" class="btn btn-primary btn-block btn-lg" style="margin-top:12px;">
            🛍️ Place Order Now (${formatCurrency(grandTotal)})
          </button>
        </form>
      </div>
    </div>
  `;
}

function toggleDeliveryInputs() {
  const type = document.getElementById('orderDeliveryType').value;
  const addressGroup = document.getElementById('addressGroup');
  const addressInput = document.getElementById('orderAddress');
  const pickupGroup = document.getElementById('pickupGroup');
  const pickupInput = document.getElementById('orderPickupDate');

  if (type === 'HOME_DELIVERY') {
    addressGroup.style.display = 'block';
    addressInput.required = true;
    pickupGroup.style.display = 'none';
    pickupInput.required = false;
  } else {
    addressGroup.style.display = 'none';
    addressInput.required = false;
    pickupGroup.style.display = 'block';
    pickupInput.required = true;
  }
}

async function updateCartItemQty(productId, newQty) {
  if (newQty <= 0) {
    await removeCartItem(productId);
    return;
  }
  try {
    await api.put(`/customer/cart/update?productId=${productId}&quantity=${newQty}`);
    await loadCart();
  } catch (err) {
    api.showToast(err.message || 'Failed to update quantity', 'error');
  }
}

async function removeCartItem(productId) {
  if (!confirm('Remove this product from your cart?')) return;
  try {
    await api.delete(`/customer/cart/remove?productId=${productId}`);
    api.showToast('Item removed from cart', 'info');
    await loadCart();
  } catch (err) {
    api.showToast(err.message || 'Failed to remove item', 'error');
  }
}

async function clearEntireCart() {
  if (!confirm('Are you sure you want to clear your entire cart?')) return;
  try {
    await api.delete('/customer/cart/clear');
    api.showToast('Cart cleared', 'info');
    await loadCart();
  } catch (err) {
    api.showToast(err.message || 'Failed to clear cart', 'error');
  }
}

async function handlePlaceOrder(e) {
  e.preventDefault();
  const deliveryType = document.getElementById('orderDeliveryType').value;
  const deliveryAddress = document.getElementById('orderAddress').value;
  const pickupDate = document.getElementById('orderPickupDate').value;

  const payload = {
    deliveryType,
    deliveryAddress: deliveryType === 'HOME_DELIVERY' ? deliveryAddress : null,
    pickupDate: deliveryType === 'STORE_PICKUP' && pickupDate ? pickupDate : null
  };

  try {
    const order = await api.post('/customer/orders', payload);
    api.showToast(`Order #${order.id} placed successfully!`, 'success');
    updateCartBadge();
    setTimeout(() => {
      window.location.href = `/order-details.html?id=${order.id}`;
    }, 1200);
  } catch (err) {
    api.showToast(err.message || 'Checkout failed. Please check product stock.', 'error');
  }
}
