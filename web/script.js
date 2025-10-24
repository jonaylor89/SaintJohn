// Set dynamic copyright year
document.getElementById('copyright-year').textContent = new Date().getFullYear();

// Salvation Mode Toggle
const salvationToggle = document.getElementById('salvation-toggle');
const body = document.body;

// Check localStorage for saved preference
const salvationMode = localStorage.getItem('salvationMode') === 'true';
if (salvationMode) {
    body.classList.add('salvation-mode');
    salvationToggle.innerHTML = '✝ Return to Earth';
}

salvationToggle.addEventListener('click', () => {
    body.classList.toggle('salvation-mode');
    const isActive = body.classList.contains('salvation-mode');

    // Update button text
    salvationToggle.innerHTML = isActive ? '✝ Return to Earth' : '✝ Enter Salvation';

    // Save preference
    localStorage.setItem('salvationMode', isActive);

    // Add a subtle effect
    body.style.transition = 'all 0.5s ease';
});

// Smooth scroll for anchor links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        e.preventDefault();
        const target = document.querySelector(this.getAttribute('href'));
        if (target) {
            target.scrollIntoView({
                behavior: 'smooth',
                block: 'start'
            });
        }
    });
});

// Add scroll event listener for header shadow on scroll
let lastScroll = 0;
const header = document.querySelector('header');

window.addEventListener('scroll', () => {
    const currentScroll = window.pageYOffset;

    if (currentScroll > 50) {
        header.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.05)';
    } else {
        header.style.boxShadow = 'none';
    }

    lastScroll = currentScroll;
});

// Intersection Observer for fade-in animations
const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
};

const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
        if (entry.isIntersecting) {
            entry.target.style.opacity = '1';
            entry.target.style.transform = 'translateY(0)';
        }
    });
}, observerOptions);

// Observe all features and showcase items for animation
document.addEventListener('DOMContentLoaded', () => {
    const animatedElements = document.querySelectorAll('.feature, .showcase-item, .philosophy');

    animatedElements.forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(30px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        observer.observe(el);
    });
});
