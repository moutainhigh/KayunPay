(function(w) {
  var circleProgress = {}
  circleProgress.refresh = function() {
    var circles = document.querySelectorAll('.circle-progress')
    for (var i = 0, len = circles.length; i < len; i++) {
      (function(i) {
        var c = circles[i]
        
        if (c.getAttribute('loaded')) return false
        
        var progress = c.getAttribute('data-progress'),
        size = c.getAttribute('data-size'),
        svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg'),
        div = document.createElement('div'),
        span = document.createElement('span'),
        circle1 = document.createElementNS('http://www.w3.org/2000/svg', 'circle'),
        circle2 = document.createElementNS('http://www.w3.org/2000/svg', 'circle')

        c.style.width = size + 'px', c.style.height = size + 'px', c.style.position = 'relative'
        
        var radius = size / 2,
          stroke = size / 20,
          C = 2 * 2.83 * radius
        svg.setAttribute('width', size)
        svg.setAttribute('height', size)
        circle1.setAttribute('cx', radius), circle2.setAttribute('cx', radius)
        circle1.setAttribute('cy', radius), circle2.setAttribute('cy', radius)
        circle1.setAttribute('r', radius - stroke), circle2.setAttribute('r', radius - stroke)
        circle1.setAttribute('stroke', '#FFEDEE'), circle2.setAttribute('stroke', '#FF4F51')
        circle1.setAttribute('stroke-width', stroke), circle2.setAttribute('stroke-width', stroke)
        circle1.setAttribute('fill', 'none'), circle2.setAttribute('fill', 'none')
        circle2.setAttribute('stroke-dasharray', 0 + ' ' + (C + 1))
        circle2.style.transition = 'stroke-dasharray .5s cubic-bezier(0.165, 0.84, 0.44, 1)'
        svg.appendChild(circle1)
        svg.appendChild(circle2)

        ;(function(circle, percent) {
          setTimeout(function() {
            circle.setAttribute('stroke-dasharray', percent + ' ' + (C + 1))
          }, 30)
        })(circle2, C * progress / 100)

        div.appendChild(svg)
        div.style.transform = 'rotate(-90deg)'
        span.style.cssText = '\
        width: ' + (radius - stroke) * 2 + 'px;\
        display: block;\
        text-align: center;\
        position: absolute;\
        top: 50%;\
        transform: translate3d(-50%, -50%, 0);\
        left: 50%; font-size: ' + size / 4 + 'px;\
        '
        span.innerText = progress + '%'

        c.appendChild(div)
        c.appendChild(span)
        c.setAttribute('loaded', true)
      })(i)
    }
  }
  w.circleProgress = circleProgress
})(window)