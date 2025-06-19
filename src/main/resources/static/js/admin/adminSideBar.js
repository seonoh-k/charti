function openSidebar() {
    document.getElementById("sidebar").classList.remove("hidden");
    document.getElementById("hamburgerBtn").classList.add("hidden");
}
function closeSidebar() {
    document.getElementById("sidebar").classList.add("hidden");
    document.getElementById("hamburgerBtn").classList.remove("hidden");
}



function toggleAccordion(openId) {
    const allMenus = ['menu1', 'menu2', 'menu3', 'menu4', 'menu5', 'menu6'];
    allMenus.forEach(id => {
        const el = document.getElementById(id);
        if (id === openId) {
            el.classList.toggle('hidden');
        } else {
            el.classList.add('hidden');
        }
    });
}