// Vanilla JS Color Picker - ersetzt Farbtastic
class ColorPicker {
    constructor(container, callback) {
        this.container = typeof container === 'string' ? document.querySelector(container) : container;
        this.callback = callback;
        this.color = '#000000';
        this.init();
    }
    
    init() {
        const input = document.createElement('input');
        input.type = 'color';
        input.value = this.color;
        input.style.cssText = 'width: 100%; height: 150px; cursor: pointer; border: none; padding: 0;';
        
        input.addEventListener('input', (e) => {
            this.color = e.target.value;
            if (this.callback) {
                this.callback(this.color);
            }
        });
        
        this.container.innerHTML = '';
        this.container.appendChild(input);
        this.container._colorpicker = this;
        this.input = input;
    }
    
    setColor(color) {
        this.color = color;
        this.input.value = color;
    }
    
    getColor() {
        return this.color;
    }
}

// jQuery-kompatible API
if (typeof $ !== 'undefined') {
    $.fn.colorpicker = function(callback) {
        return this.each(function() {
            new ColorPicker(this, callback);
        });
    };
    
    $.colorpicker = function(container, callback) {
        return new ColorPicker(container, callback);
    };
}

// Export für globale Verwendung
window.ColorPicker = ColorPicker;
