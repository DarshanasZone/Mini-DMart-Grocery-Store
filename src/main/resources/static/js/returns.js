/**
 * Mini-DMart Customer Returns Script
 */
document.addEventListener('DOMContentLoaded', () => {
  if (document.getElementById('returnsHistoryContainer')) {
    initReturnsPage();
  }
});

async function initReturnsPage() {
  const user = api.getUser();
  if (!user) {
    window.location.href = `/login.html?redirect=${encodeURIComponent(window.location.pathname)}`;
    return;
  }

  // Pre-fill Order ID from URL if navigated from order details
  const params = new URLSearchParams(window.location.search);
  const orderId = params.get('orderId');
  if (orderId && document.getElementById('returnOrderIdInput')) {
    document.getElementById('returnOrderIdInput').value = orderId;
  }

  await loadReturnsHistory();
}

async function loadReturnsHistory() {
  const container = document.getElementById('returnsHistoryContainer');
  if (!container) return;

  try {
    const returns = await api.get('/customer/returns');
    if (!returns || returns.length === 0) {
      container.innerHTML = `
        <div style="text-align:center; padding:40px; color:var(--gray-500);">
          No return requests submitted yet.
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
              <th>Reason</th>
              <th>Status</th>
              <th>Requested On</th>
            </tr>
          </thead>
          <tbody>
            ${returns.map(r => `
              <tr>
                <td><strong>#${r.id}</strong></td>
                <td><a href="/order-details.html?id=${r.orderId}">#${r.orderId}</a></td>
                <td>${escapeHtml(r.reason)}</td>
                <td><span class="badge return-status-${r.status}">${escapeHtml(r.status)}</span></td>
                <td>${r.createdAt ? new Date(r.createdAt).toLocaleDateString('en-IN') : 'N/A'}</td>
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    `;
  } catch (err) {
    container.innerHTML = `<p style="color:var(--danger); padding:20px;">Failed to load return history.</p>`;
  }
}

async function handleReturnSubmit(e) {
  e.preventDefault();
  const orderId = document.getElementById('returnOrderIdInput').value;
  const reason = document.getElementById('returnReasonInput').value;

  try {
    await api.post('/customer/returns', {
      orderId: parseInt(orderId),
      reason
    });
    api.showToast('Return request submitted for admin review.', 'success');
    document.getElementById('returnForm').reset();
    await loadReturnsHistory();
  } catch (err) {
    api.showToast(err.message || 'Failed to submit return request', 'error');
  }
}
