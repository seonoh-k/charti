document.addEventListener("DOMContentLoaded", function() {
    ['flashMsg','flashError'].forEach(id => {
        const el = document.getElementById(id);
        if(el) {
            setTimeout(() => {
                el.classList.add('transition-opacity', 'duration-700');
                el.classList.add('opacity-0');
                setTimeout(() => el.style.display='none', 700);
            }, 3000);
        }
    });
});