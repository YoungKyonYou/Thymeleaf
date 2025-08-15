// // window.Grid.init(container, options?)
// (function () {
//     function init(container, options) {
//         const base = container.dataset.base; // e.g. "/users"
//         const endpoints = options?.endpoints || defaultEndpoints(base);
//
//         // 모달 관련
//         const modalEl   = document.getElementById(container.dataset.modalId || 'formModal');
//         const modalBody = document.getElementById(container.dataset.modalBodyId || 'modal-body');
//         const modal     = modalEl && window.bootstrap ? new bootstrap.Modal(modalEl) : null;
//
//         bindGrid(container, {
//             base,
//             listUrl: container.dataset.listUrl || base,
//             endpoints,
//             btnNew: document.getElementById(container.dataset.btnNewId || 'btn-new'),
//             modal, modalBody
//         });
//     }
//
//     window.Grid = { init };
// })();