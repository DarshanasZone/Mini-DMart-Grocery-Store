/**
 * Mini-DMart Products & Catalog Script
 */
let allProducts = [];
let allCategories = [];
let selectedCategoryId = null;

document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('productCatalogGrid')) {
    initCatalog();
  }
  if (document.getElementById('productDetailPage')) {
    initProductDetail();
  }
  if (document.getElementById('featuredProductsGrid')) {
    initFeaturedProducts();
  }
});

async function initCatalog() {
  await loadCategories();
  const params = new URLSearchParams(window.location.search);
  const q = params.get('q');
  const catId = params.get('categoryId');

  if (q) {
    const searchInput = document.getElementById('catalogSearchInput');
    if (searchInput) searchInput.value = q;
    await loadProducts(null, q);
  } else if (catId) {
    selectedCategoryId = parseInt(catId);
    await loadProducts(selectedCategoryId, null);
  } else {
    await loadProducts();
  }
  setupSearchAndFilters();
}

async function initFeaturedProducts() {
  try {
    const products = await api.get('/customer/products');
    const container = document.getElementById('featuredProductsGrid');
    if (!container) return;

    if (!products || products.length === 0) {
      container.innerHTML = `<p style="grid-column:1/-1; text-align:center; color:var(--gray-500); padding:40px;">No products available right now.</p>`;
      return;
    }

    const featured = products.slice(0, 8);
    container.innerHTML = featured.map(p => renderProductCard(p)).join('');
  } catch (err) {
    console.error('Error loading featured products:', err);
  }
}

async function loadCategories() {
  try {
    allCategories = await api.get('/categories');
    const container = document.getElementById('categoryRibbonPills');
    if (container) {
      container.innerHTML = `
        <button class="cat-pill active" onclick="filterByCategory(null, this)">All Departments</button>
        ${allCategories.map(c => `
          <button class="cat-pill" onclick="filterByCategory(${c.id}, this)">${escapeHtml(c.name)}</button>
        `).join('')}
      `;
    }
  } catch (err) {
    console.error('Failed to load categories:', err);
  }
}

async function loadProducts(categoryId = null, query = null) {
  const container = document.getElementById('productCatalogGrid');
  if (!container) return;

  container.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:50px;">
    <p style="font-size:1.1rem; color:var(--gray-500);">Loading fresh groceries...</p>
  </div>`;

  try {
    let endpoint = '/customer/products';
    if (categoryId) {
      endpoint = `/customer/products/category/${categoryId}`;
    } else if (query && query.trim()) {
      endpoint = `/customer/products/search?name=${encodeURIComponent(query.trim())}`;
    }

    allProducts = await api.get(endpoint);
    renderProductsList(allProducts);
  } catch (err) {
    container.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:50px; color:var(--danger);">
      <p>⚠️ Failed to load products. Please try again later.</p>
    </div>`;
  }
}

function renderProductsList(products) {
  const container = document.getElementById('productCatalogGrid');
  if (!container) return;

  if (!products || products.length === 0) {
    container.innerHTML = `
      <div style="grid-column:1/-1; text-align:center; padding:60px 20px; background:var(--white); border-radius:var(--radius-md); border:1px solid var(--gray-200);">
        <p style="font-size:2.5rem; margin-bottom:10px;">🛒</p>
        <h3 style="color:var(--dark); margin-bottom:8px;">No Groceries Found</h3>
        <p style="color:var(--gray-500);">Try searching for something else or browse another department.</p>
      </div>
    `;
    return;
  }

  container.innerHTML = products.map(p => renderProductCard(p)).join('');
}

function renderProductCard(p) {
  const isOutOfStock = !p.stockQuantity || p.stockQuantity <= 0;
  const isLowStock = p.stockQuantity > 0 && p.stockQuantity <= 5;
  const stockClass = isOutOfStock ? 'out-of-stock' : isLowStock ? 'low-stock' : 'in-stock';
  const stockLabel = isOutOfStock ? 'Out of Stock' : isLowStock ? `Only ${p.stockQuantity} Left` : 'In Stock';

  const defaultImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=600&auto=format&fit=crop&q=80';
  const imgUrl = p.imageUrl && p.imageUrl.trim() ? p.imageUrl : defaultImg;

  return `
    <div class="product-card">
      <div class="product-img-wrap">
        <span class="stock-tag ${stockClass}">${stockLabel}</span>
        <img src="${escapeHtml(imgUrl)}" alt="${escapeHtml(p.name)}" class="product-img" onerror="this.src='${defaultImg}'" />
      </div>
      <div class="product-body">
        <span class="product-category">${escapeHtml(p.category ? p.category.name : 'Grocery')}</span>
        <a href="/product-details.html?id=${p.id}" class="product-title">${escapeHtml(p.name)}</a>
        <p class="product-desc">${escapeHtml(p.description || '')}</p>
        <div class="product-foot">
          <div class="product-price">${formatCurrency(p.price)}</div>
          <button class="add-btn" onclick="addToCartDirect(${p.id}, 1)" ${isOutOfStock ? 'disabled' : ''}>
            ${isOutOfStock ? 'Sold Out' : '+ Add to Cart'}
          </button>
        </div>
      </div>
    </div>
  `;
}

function filterByCategory(catId, btnElement) {
  selectedCategoryId = catId;
  const pills = document.querySelectorAll('.cat-pill');
  pills.forEach(p => p.classList.remove('active'));
  if (btnElement) btnElement.classList.add('active');

  const searchInput = document.getElementById('catalogSearchInput');
  if (searchInput) searchInput.value = '';

  loadProducts(catId, null);
}

function setupSearchAndFilters() {
  const searchInput = document.getElementById('catalogSearchInput');
  if (!searchInput) return;

  let debounceTimer;
  searchInput.addEventListener('input', (e) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      const q = e.target.value.trim();
      if (q) {
        selectedCategoryId = null;
        document.querySelectorAll('.cat-pill').forEach(p => p.classList.remove('active'));
        loadProducts(null, q);
      } else {
        loadProducts(selectedCategoryId, null);
      }
    }, 350);
  });
}

async function addToCartDirect(productId, quantity = 1) {
  const user = api.getUser();
  if (!user) {
    api.showToast('Please log in to add items to your cart.', 'info');
    setTimeout(() => {
      window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname)}`;
    }, 1000);
    return;
  }

  try {
    await api.post(`/customer/cart/add?productId=${productId}&quantity=${quantity}`);
    api.showToast('Added to cart successfully!', 'success');
    updateCartBadge();
  } catch (err) {
    api.showToast(err.message || 'Failed to add to cart', 'error');
  }
}

async function initProductDetail() {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  const container = document.getElementById('productDetailPage');
  if (!id || !container) return;

  try {
    const product = await api.get(`/customer/products/${id}`);
    const isOutOfStock = !product.stockQuantity || product.stockQuantity <= 0;
    const defaultImg = 'https://images.unsplash.com/photo-1542838132-92c53300491e?w=600&auto=format&fit=crop&q=80';
    const imgUrl = product.imageUrl && product.imageUrl.trim() ? product.imageUrl : defaultImg;

    container.innerHTML = `
      <div style="background:var(--white); border-radius:var(--radius-lg); border:1px solid var(--gray-200); padding:36px; display:grid; grid-template-columns:1fr 1fr; gap:40px; box-shadow:var(--shadow-sm);">
        <div style="background:#f8fafc; border-radius:var(--radius-md); overflow:hidden; display:flex; align-items:center; justify-content:center; max-height:420px;">
          <img src="${escapeHtml(imgUrl)}" alt="${escapeHtml(product.name)}" style="width:100%; height:100%; object-fit:cover;" onerror="this.src='${defaultImg}'" />
        </div>
        <div>
          <div class="badge badge-info" style="margin-bottom:12px;">${escapeHtml(product.category ? product.category.name : 'Grocery')}</div>
          <h1 style="font-size:1.8rem; font-weight:800; color:var(--dark); margin-bottom:12px;">${escapeHtml(product.name)}</h1>
          <div style="font-size:2rem; font-weight:800; color:var(--primary); margin-bottom:16px;">${formatCurrency(product.price)}</div>
          
          <div style="margin-bottom:20px; padding:16px; background:var(--gray-100); border-radius:var(--radius-sm);">
            <h4 style="font-size:0.9rem; margin-bottom:6px; color:var(--gray-700);">Stock Availability:</h4>
            <p style="font-weight:700; color:${isOutOfStock ? 'var(--danger)' : 'var(--primary)'};">
              ${isOutOfStock ? '❌ Currently Out of Stock' : `✅ In Stock (${product.stockQuantity} units available)`}
            </p>
          </div>

          <p style="color:var(--gray-700); font-size:1rem; line-height:1.7; margin-bottom:24px;">
            ${escapeHtml(product.description || 'Fresh quality grocery product from Mini-DMart.')}
          </p>

          <div style="display:flex; align-items:center; gap:16px; margin-bottom:24px;">
            <label style="font-weight:600;">Quantity:</label>
            <div style="display:flex; align-items:center; border:1.5px solid var(--gray-300); border-radius:var(--radius-sm); overflow:hidden;">
              <button class="btn btn-outline" style="border:none; padding:8px 14px;" onclick="changeQty(-1)">-</button>
              <input type="number" id="detailQtyInput" value="1" min="1" max="${product.stockQuantity || 1}" style="width:50px; text-align:center; border:none; outline:none; font-weight:700;" />
              <button class="btn btn-outline" style="border:none; padding:8px 14px;" onclick="changeQty(1)">+</button>
            </div>
          </div>

          <div style="display:flex; gap:12px;">
            <button class="btn btn-primary btn-lg" style="flex:1;" onclick="addDetailToCart(${product.id})" ${isOutOfStock ? 'disabled' : ''}>
              🛒 Add to Cart
            </button>
            <a href="/products.html" class="btn btn-outline btn-lg">Back to Catalog</a>
          </div>
        </div>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<div style="text-align:center; padding:60px; color:var(--danger);">Product not found or unavailable.</div>`;
  }
}

function changeQty(delta) {
  const input = document.getElementById('detailQtyInput');
  if (!input) return;
  let val = parseInt(input.value) || 1;
  const max = parseInt(input.max) || 999;
  val = Math.max(1, Math.min(val + delta, max));
  input.value = val;
}

function addDetailToCart(productId) {
  const input = document.getElementById('detailQtyInput');
  const qty = input ? parseInt(input.value) || 1 : 1;
  addToCartDirect(productId, qty);
}
