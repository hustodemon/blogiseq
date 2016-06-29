function toggleMenu() {
    var yourUl = document.getElementById('menu-inner');
    yourUl.style.display = (yourUl.style.display === 'none' || yourUl.style.display === '') ? 'block' : 'none';
}

document.addEventListener('DOMContentLoaded', function(event) {
    document.getElementById('menu').addEventListener('click', toggleMenu);
});
