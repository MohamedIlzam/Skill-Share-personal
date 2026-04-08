// Shared PWA Logic for SkillShare
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/sw.js').catch(err => {
            console.log('SW registration failed: ', err);
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    // iPhone PWA Detection
    const isIos = /iPad|iPhone|iPod/.test(navigator.userAgent) && !window.MSStream;
    const isStandalone = ('standalone' in window.navigator) && (window.navigator.standalone) || window.matchMedia('(display-mode: standalone)').matches;

    const prompt = document.getElementById('ios-install-prompt');
    if (isIos && !isStandalone && prompt) {
        // Show hint to add to home screen if on iPhone Safari
        setTimeout(() => {
            prompt.style.display = 'block';
        }, 3000);
    }
});
