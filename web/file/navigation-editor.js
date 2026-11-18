(function () {
  var form = document.getElementById('navigation-form');
  var list = document.getElementById('navigation-items');
  var filters = document.getElementById('navigation-filters');
  var announcement = document.getElementById('navigation-announcement');
  var activeFilter = 'all';

  function items() {
    return Array.from(list.querySelectorAll('[data-navigation-item]'));
  }

  function withName(template, name) {
    return template.replace('%s', function () { return name; });
  }

  function update() {
    var rows = items();
    rows.forEach(function (row) {
      var group = rows.filter(function (candidate) { return candidate.dataset.group === row.dataset.group; });
      var index = group.indexOf(row);
      var name = row.querySelector('[name=nav_label]').value.trim() || row.dataset.defaultLabel;
      var visible = row.querySelector('[name=nav_enabled]').value === '1';
      row.querySelector('[data-navigation-name]').textContent = name;
      row.querySelector('[data-navigation-status]').textContent = visible ? form.dataset.visible : form.dataset.hidden;
      row.querySelector('[data-move=up]').disabled = index === 0;
      row.querySelector('[data-move=down]').disabled = index === group.length - 1;
      row.querySelector('[data-move=up]').setAttribute('aria-label', withName(form.dataset.moveUp, name));
      row.querySelector('[data-move=down]').setAttribute('aria-label', withName(form.dataset.moveDown, name));
      var remove = row.querySelector('[data-remove]');
      if (remove) remove.setAttribute('aria-label', withName(form.dataset.remove, name));
      row.classList.toggle('d-none', activeFilter !== 'all' && activeFilter !== row.dataset.group);
    });
    filters.querySelectorAll('[data-filter]').forEach(function (button) {
      var selected = button.dataset.filter === activeFilter;
      button.setAttribute('aria-pressed', String(selected));
      button.classList.toggle('btn-secondary', selected);
      button.classList.toggle('btn-outline-secondary', !selected);
    });
  }

  filters.hidden = false;
  filters.addEventListener('click', function (event) {
    var button = event.target.closest('[data-filter]');
    if (!button) return;
    activeFilter = button.dataset.filter;
    update();
  });

  document.getElementById('navigation-add').addEventListener('click', function () {
    var row = document.getElementById('navigation-template').content.firstElementChild.cloneNode(true);
    var bytes = new Uint8Array(16);
    crypto.getRandomValues(bytes);
    var hex = Array.from(bytes, function (b) { return b.toString(16).padStart(2, '0'); }).join('');
    row.querySelector('[name=nav_id]').value = 'custom-' + hex.slice(0, 8) + '-' + hex.slice(8, 12) + '-' + hex.slice(12, 16) + '-' + hex.slice(16, 20) + '-' + hex.slice(20);
    var main = items().filter(function (item) { return item.dataset.group === 'main'; });
    list.insertBefore(row, main.length ? main[main.length - 1].nextElementSibling : list.firstElementChild);
    activeFilter = 'main';
    update();
    row.querySelector('[name=nav_label]').focus();
    announcement.textContent = form.dataset.added;
  });

  list.addEventListener('click', function (event) {
    var button = event.target.closest('button');
    if (!button) return;
    var row = button.closest('[data-navigation-item]');
    var name = row.querySelector('[data-navigation-name]').textContent;
    if (button.hasAttribute('data-remove')) {
      row.remove();
      update();
      announcement.textContent = withName(form.dataset.removed, name);
      document.getElementById('navigation-add').focus();
      return;
    }
    if (!button.dataset.move) return;
    var rows = items().filter(function (item) { return item.dataset.group === row.dataset.group; });
    var index = rows.indexOf(row);
    if (button.dataset.move === 'up' && index > 0) list.insertBefore(row, rows[index - 1]);
    if (button.dataset.move === 'down' && index < rows.length - 1) list.insertBefore(rows[index + 1], row);
    update();
    var nextFocus = button.disabled ? row.querySelector('[data-move=' + (button.dataset.move === 'up' ? 'down' : 'up') + ']') : button;
    nextFocus.focus();
    announcement.textContent = withName(form.dataset.moved, name);
  });

  list.addEventListener('input', update);
  list.addEventListener('change', update);
  form.addEventListener('invalid', function (event) {
    var row = event.target.closest('[data-navigation-item]');
    if (row) {
      activeFilter = row.dataset.group;
      update();
    }
  }, true);
  update();
}());
