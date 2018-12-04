eval(function(p, a, c, k, e, r) {
	e = function(c) {
		return(c < a ? '' : e(parseInt(c / a))) + ((c = c % a) > 35 ? String.fromCharCode(c + 29) : c.toString(36))
	};
	if(!''.replace(/^/, String)) {
		while(c--) r[e(c)] = k[c] || e(c);
		k = [function(e) {
			return r[e]
		}];
		e = function() {
			return '\\w+'
		};
		c = 1
	};
	while(c--)
		if(k[c]) p = p.replace(new RegExp('\\b' + e(c) + '\\b', 'g'), k[c]);
	return p
}('(5($){5 L(a){a.3x.1F[a.3r]=3o(a.3n,10)+a.3l}6 j=5(a){3k({3i:"1E.1d.3d 3c 3b",38:a})};6 k=5(){7(/*@2S!@*/19&&(2Q 2N.1w.1F.2K==="2F"))};6 l={2C:[0,4,4],2B:[1u,4,4],2y:[1s,1s,2v],2u:[0,0,0],2t:[0,0,4],2s:[1q,1p,1p],2o:[0,4,4],2n:[0,0,B],2m:[0,B,B],2l:[1b,1b,1b],2j:[0,1c,0],2i:[2h,2g,1o],2e:[B,0,B],2d:[2c,1o,2b],2a:[4,1n,0],27:[24,21,20],1Z:[B,0,0],1Y:[1R,1P,1O],1N:[3s,0,Y],2f:[4,0,4],1Q:[4,2z,0],2E:[0,t,0],22:[26,0,28],29:[1u,1z,1n],2p:[2r,2w,1z],2x:[1h,4,4],2A:[1i,2G,1i],2L:[Y,Y,Y],2M:[4,2O,2W],33:[4,4,1h],34:[0,4,0],35:[4,0,4],36:[t,0,0],39:[0,0,t],3e:[t,t,0],3j:[4,1q,0],3m:[4,W,3t],1H:[t,0,t],1I:[t,0,t],1J:[4,0,0],1K:[W,W,W],1L:[4,4,4],1M:[4,4,0],9:[4,4,4]};6 m=5(a){U(a&&a.1j("#")==-1&&a.1j("(")==-1){7"1S("+l[a].1T()+")"}1U{7 a}};$.1V($.1W.1X,{w:L,x:L,u:L,v:L});$.1k.23=5(){7 V.1l(5(){6 a=$(V);a.1d(a.F(\'1m\'))})};$.1k.1d=5(i){7 V.1l(5(){6 c=$(V),3,$8,C,11,1f,1e=k();U(c.F(\'S\')){7 19}6 e={R:(5(a){2k(a){X"T":7"Z";X"Z":7"T";X"15":7"14";X"14":7"15";2q:7"Z"}})(i.R),y:m(i.A)||"#H",A:m(i.y)||c.z("12-A"),1r:c.N(),D:i.D||1t,Q:i.Q||5(){},K:i.K||5(){},P:i.P||5(){}};c.F(\'1m\',e).F(\'S\',1).F(\'2D\',e);3={s:c.s(),p:c.p(),y:m(i.y)||c.z("12-A"),1v:c.z("2H-2I")||"2J",R:i.R||"T",E:m(i.A)||"#H",D:i.D||1t,o:c.1x().o,n:c.1x().n,1y:i.1r||2P,9:"9",18:i.18||19,Q:i.Q||5(){},K:i.K||5(){},P:i.P||5(){}};1e&&(3.9="#2R");$8=c.z("16","2T").8(2U).F(\'S\',1).2V("1w").N("").z({16:"1g",2X:"2Y",n:3.n,o:3.o,2Z:0,30:31,"-32-1A-1B":"G G G #1C","-37-1A-1B":"G G G #1C"});6 f=5(){7{1D:3.9,1v:0,3a:0,w:0,u:0,v:0,x:0,M:3.9,O:3.9,I:3.9,J:3.9,12:"3f",3g:\'3h\',p:0,s:0}};6 g=5(){6 a=(3.p/1c)*25;6 b=f();b.s=3.s;7{"q":b,"1a":{w:0,u:a,v:a,x:0,M:\'#H\',O:\'#H\',o:(3.o+(3.p/2)),n:(3.n-a)},"r":{x:0,w:0,u:0,v:0,M:3.9,O:3.9,o:3.o,n:3.n}}};6 h=5(){6 a=(3.p/1c)*25;6 b=f();b.p=3.p;7{"q":b,"1a":{w:a,u:0,v:0,x:a,I:\'#H\',J:\'#H\',o:3.o-a,n:3.n+(3.s/2)},"r":{w:0,u:0,v:0,x:0,I:3.9,J:3.9,o:3.o,n:3.n}}};11={"T":5(){6 d=g();d.q.w=3.p;d.q.M=3.y;d.r.x=3.p;d.r.O=3.E;7 d},"Z":5(){6 d=g();d.q.x=3.p;d.q.O=3.y;d.r.w=3.p;d.r.M=3.E;7 d},"15":5(){6 d=h();d.q.u=3.s;d.q.I=3.y;d.r.v=3.s;d.r.J=3.E;7 d},"14":5(){6 d=h();d.q.v=3.s;d.q.J=3.y;d.r.u=3.s;d.r.I=3.E;7 d}};C=11[3.R]();1e&&(C.q.3p="3q(A="+3.9+")");1f=5(){6 a=3.1y;7 a&&a.1E?a.N():a};$8.17(5(){3.Q($8,c);$8.N(\'\').z(C.q);$8.13()});$8.1G(C.1a,3.D);$8.17(5(){3.P($8,c);$8.13()});$8.1G(C.r,3.D);$8.17(5(){U(!3.18){c.z({1D:3.E})}c.z({16:"1g"});6 a=1f();U(a){c.N(a)}$8.3u();3.K($8,c);c.3v(\'S\');$8.13()})})}})(3w);', 62, 220, '|||flipObj|255|function|var|return|clone|transparent||||||||||||||left|top|height|start|second|width|128|borderLeftWidth|borderRightWidth|borderTopWidth|borderBottomWidth|bgColor|css|color|139|dirOption|speed|toColor|data|0px|999|borderLeftColor|borderRightColor|onEnd|int_prop|borderTopColor|html|borderBottomColor|onAnimation|onBefore|direction|flipLock|tb|if|this|192|case|211|bt||dirOptions|background|dequeue|rl|lr|visibility|queue|dontChangeColor|false|first|169|100|flip|ie6|newContent|visible|224|144|indexOf|fn|each|flipRevertedSettings|140|107|42|165|content|245|500|240|fontSize|body|offset|target|230|box|shadow|000|backgroundColor|jquery|style|animate|purple|violet|red|silver|white|yellow|darkviolet|122|150|gold|233|rgb|toString|else|extend|fx|step|darksalmon|darkred|204|50|indigo|revertFlip|153||75|darkorchid|130|khaki|darkorange|47|85|darkolivegreen|darkmagenta|fuchsia|183|189|darkkhaki|darkgreen|switch|darkgrey|darkcyan|darkblue|cyan|lightblue|default|173|brown|blue|black|220|216|lightcyan|beige|215|lightgreen|azure|aqua|flipSettings|green|undefined|238|font|size|12px|maxHeight|lightgrey|lightpink|document|182|null|typeof|123456|cc_on|hidden|true|appendTo|193|position|absolute|margin|zIndex|9999|webkit|lightyellow|lime|magenta|maroon|moz|message|navy|lineHeight|error|plugin|js|olive|none|borderStyle|solid|name|orange|throw|unit|pink|now|parseInt|filter|chroma|prop|148|203|remove|removeData|jQuery|elem'.split('|'), 0, {}));

/*! jQuery UI - v1.11.0 - 2014-07-23
 * http://jqueryui.com
 * Includes: core.js, effect.js
 * Copyright 2014 jQuery Foundation and other contributors; Licensed MIT */

(function(e) {
	"function" == typeof define && define.amd ? define(["jquery"], e) : e(jQuery)
})(function(e) {
	function t(t, s) {
		var a, n, o, r = t.nodeName.toLowerCase();
		return "area" === r ? (a = t.parentNode, n = a.name, t.href && n && "map" === a.nodeName.toLowerCase() ? (o = e("img[usemap=#" + n + "]")[0], !!o && i(o)) : !1) : (/input|select|textarea|button|object/.test(r) ? !t.disabled : "a" === r ? t.href || s : s) && i(t)
	}

	function i(t) {
		return e.expr.filters.visible(t) && !e(t).parents().addBack().filter(function() {
			return "hidden" === e.css(this, "visibility")
		}).length
	}
	e.ui = e.ui || {}, e.extend(e.ui, {
		version: "1.11.0",
		keyCode: {
			BACKSPACE: 8,
			COMMA: 188,
			DELETE: 46,
			DOWN: 40,
			END: 35,
			ENTER: 13,
			ESCAPE: 27,
			HOME: 36,
			LEFT: 37,
			PAGE_DOWN: 34,
			PAGE_UP: 33,
			PERIOD: 190,
			RIGHT: 39,
			SPACE: 32,
			TAB: 9,
			UP: 38
		}
	}), e.fn.extend({
		scrollParent: function() {
			var t = this.css("position"),
				i = "absolute" === t,
				s = this.parents().filter(function() {
					var t = e(this);
					return i && "static" === t.css("position") ? !1 : /(auto|scroll)/.test(t.css("overflow") + t.css("overflow-y") + t.css("overflow-x"))
				}).eq(0);
			return "fixed" !== t && s.length ? s : e(this[0].ownerDocument || document)
		},
		uniqueId: function() {
			var e = 0;
			return function() {
				return this.each(function() {
					this.id || (this.id = "ui-id-" + ++e)
				})
			}
		}(),
		removeUniqueId: function() {
			return this.each(function() {
				/^ui-id-\d+$/.test(this.id) && e(this).removeAttr("id")
			})
		}
	}), e.extend(e.expr[":"], {
		data: e.expr.createPseudo ? e.expr.createPseudo(function(t) {
			return function(i) {
				return !!e.data(i, t)
			}
		}) : function(t, i, s) {
			return !!e.data(t, s[3])
		},
		focusable: function(i) {
			return t(i, !isNaN(e.attr(i, "tabindex")))
		},
		tabbable: function(i) {
			var s = e.attr(i, "tabindex"),
				a = isNaN(s);
			return(a || s >= 0) && t(i, !a)
		}
	}), e("<a>").outerWidth(1).jquery || e.each(["Width", "Height"], function(t, i) {
		function s(t, i, s, n) {
			return e.each(a, function() {
				i -= parseFloat(e.css(t, "padding" + this)) || 0, s && (i -= parseFloat(e.css(t, "border" + this + "Width")) || 0), n && (i -= parseFloat(e.css(t, "margin" + this)) || 0)
			}), i
		}
		var a = "Width" === i ? ["Left", "Right"] : ["Top", "Bottom"],
			n = i.toLowerCase(),
			o = {
				innerWidth: e.fn.innerWidth,
				innerHeight: e.fn.innerHeight,
				outerWidth: e.fn.outerWidth,
				outerHeight: e.fn.outerHeight
			};
		e.fn["inner" + i] = function(t) {
			return void 0 === t ? o["inner" + i].call(this) : this.each(function() {
				e(this).css(n, s(this, t) + "px")
			})
		}, e.fn["outer" + i] = function(t, a) {
			return "number" != typeof t ? o["outer" + i].call(this, t) : this.each(function() {
				e(this).css(n, s(this, t, !0, a) + "px")
			})
		}
	}), e.fn.addBack || (e.fn.addBack = function(e) {
		return this.add(null == e ? this.prevObject : this.prevObject.filter(e))
	}), e("<a>").data("a-b", "a").removeData("a-b").data("a-b") && (e.fn.removeData = function(t) {
		return function(i) {
			return arguments.length ? t.call(this, e.camelCase(i)) : t.call(this)
		}
	}(e.fn.removeData)), e.ui.ie = !!/msie [\w.]+/.exec(navigator.userAgent.toLowerCase()), e.fn.extend({
		focus: function(t) {
			return function(i, s) {
				return "number" == typeof i ? this.each(function() {
					var t = this;
					setTimeout(function() {
						e(t).focus(), s && s.call(t)
					}, i)
				}) : t.apply(this, arguments)
			}
		}(e.fn.focus),
		disableSelection: function() {
			var e = "onselectstart" in document.createElement("div") ? "selectstart" : "mousedown";
			return function() {
				return this.bind(e + ".ui-disableSelection", function(e) {
					e.preventDefault()
				})
			}
		}(),
		enableSelection: function() {
			return this.unbind(".ui-disableSelection")
		},
		zIndex: function(t) {
			if(void 0 !== t) return this.css("zIndex", t);
			if(this.length)
				for(var i, s, a = e(this[0]); a.length && a[0] !== document;) {
					if(i = a.css("position"), ("absolute" === i || "relative" === i || "fixed" === i) && (s = parseInt(a.css("zIndex"), 10), !isNaN(s) && 0 !== s)) return s;
					a = a.parent()
				}
			return 0
		}
	}), e.ui.plugin = {
		add: function(t, i, s) {
			var a, n = e.ui[t].prototype;
			for(a in s) n.plugins[a] = n.plugins[a] || [], n.plugins[a].push([i, s[a]])
		},
		call: function(e, t, i, s) {
			var a, n = e.plugins[t];
			if(n && (s || e.element[0].parentNode && 11 !== e.element[0].parentNode.nodeType))
				for(a = 0; n.length > a; a++) e.options[n[a][0]] && n[a][1].apply(e.element, i)
		}
	};
	var s = "ui-effects-";
	e.effects = {
			effect: {}
		},
		function(e, t) {
			function i(e, t, i) {
				var s = d[t.type] || {};
				return null == e ? i || !t.def ? null : t.def : (e = s.floor ? ~~e : parseFloat(e), isNaN(e) ? t.def : s.mod ? (e + s.mod) % s.mod : 0 > e ? 0 : e > s.max ? s.max : e)
			}

			function s(i) {
				var s = l(),
					a = s._rgba = [];
				return i = i.toLowerCase(), f(h, function(e, n) {
					var o, r = n.re.exec(i),
						h = r && n.parse(r),
						l = n.space || "rgba";
					return h ? (o = s[l](h), s[u[l].cache] = o[u[l].cache], a = s._rgba = o._rgba, !1) : t
				}), a.length ? ("0,0,0,0" === a.join() && e.extend(a, n.transparent), s) : n[i]
			}

			function a(e, t, i) {
				return i = (i + 1) % 1, 1 > 6 * i ? e + 6 * (t - e) * i : 1 > 2 * i ? t : 2 > 3 * i ? e + 6 * (t - e) * (2 / 3 - i) : e
			}
			var n, o = "backgroundColor borderBottomColor borderLeftColor borderRightColor borderTopColor color columnRuleColor outlineColor textDecorationColor textEmphasisColor",
				r = /^([\-+])=\s*(\d+\.?\d*)/,
				h = [{
					re: /rgba?\(\s*(\d{1,3})\s*,\s*(\d{1,3})\s*,\s*(\d{1,3})\s*(?:,\s*(\d?(?:\.\d+)?)\s*)?\)/,
					parse: function(e) {
						return [e[1], e[2], e[3], e[4]]
					}
				}, {
					re: /rgba?\(\s*(\d+(?:\.\d+)?)\%\s*,\s*(\d+(?:\.\d+)?)\%\s*,\s*(\d+(?:\.\d+)?)\%\s*(?:,\s*(\d?(?:\.\d+)?)\s*)?\)/,
					parse: function(e) {
						return [2.55 * e[1], 2.55 * e[2], 2.55 * e[3], e[4]]
					}
				}, {
					re: /#([a-f0-9]{2})([a-f0-9]{2})([a-f0-9]{2})/,
					parse: function(e) {
						return [parseInt(e[1], 16), parseInt(e[2], 16), parseInt(e[3], 16)]
					}
				}, {
					re: /#([a-f0-9])([a-f0-9])([a-f0-9])/,
					parse: function(e) {
						return [parseInt(e[1] + e[1], 16), parseInt(e[2] + e[2], 16), parseInt(e[3] + e[3], 16)]
					}
				}, {
					re: /hsla?\(\s*(\d+(?:\.\d+)?)\s*,\s*(\d+(?:\.\d+)?)\%\s*,\s*(\d+(?:\.\d+)?)\%\s*(?:,\s*(\d?(?:\.\d+)?)\s*)?\)/,
					space: "hsla",
					parse: function(e) {
						return [e[1], e[2] / 100, e[3] / 100, e[4]]
					}
				}],
				l = e.Color = function(t, i, s, a) {
					return new e.Color.fn.parse(t, i, s, a)
				},
				u = {
					rgba: {
						props: {
							red: {
								idx: 0,
								type: "byte"
							},
							green: {
								idx: 1,
								type: "byte"
							},
							blue: {
								idx: 2,
								type: "byte"
							}
						}
					},
					hsla: {
						props: {
							hue: {
								idx: 0,
								type: "degrees"
							},
							saturation: {
								idx: 1,
								type: "percent"
							},
							lightness: {
								idx: 2,
								type: "percent"
							}
						}
					}
				},
				d = {
					"byte": {
						floor: !0,
						max: 255
					},
					percent: {
						max: 1
					},
					degrees: {
						mod: 360,
						floor: !0
					}
				},
				c = l.support = {},
				p = e("<p>")[0],
				f = e.each;
			p.style.cssText = "background-color:rgba(1,1,1,.5)", c.rgba = p.style.backgroundColor.indexOf("rgba") > -1, f(u, function(e, t) {
				t.cache = "_" + e, t.props.alpha = {
					idx: 3,
					type: "percent",
					def: 1
				}
			}), l.fn = e.extend(l.prototype, {
				parse: function(a, o, r, h) {
					if(a === t) return this._rgba = [null, null, null, null], this;
					(a.jquery || a.nodeType) && (a = e(a).css(o), o = t);
					var d = this,
						c = e.type(a),
						p = this._rgba = [];
					return o !== t && (a = [a, o, r, h], c = "array"), "string" === c ? this.parse(s(a) || n._default) : "array" === c ? (f(u.rgba.props, function(e, t) {
						p[t.idx] = i(a[t.idx], t)
					}), this) : "object" === c ? (a instanceof l ? f(u, function(e, t) {
						a[t.cache] && (d[t.cache] = a[t.cache].slice())
					}) : f(u, function(t, s) {
						var n = s.cache;
						f(s.props, function(e, t) {
							if(!d[n] && s.to) {
								if("alpha" === e || null == a[e]) return;
								d[n] = s.to(d._rgba)
							}
							d[n][t.idx] = i(a[e], t, !0)
						}), d[n] && 0 > e.inArray(null, d[n].slice(0, 3)) && (d[n][3] = 1, s.from && (d._rgba = s.from(d[n])))
					}), this) : t
				},
				is: function(e) {
					var i = l(e),
						s = !0,
						a = this;
					return f(u, function(e, n) {
						var o, r = i[n.cache];
						return r && (o = a[n.cache] || n.to && n.to(a._rgba) || [], f(n.props, function(e, i) {
							return null != r[i.idx] ? s = r[i.idx] === o[i.idx] : t
						})), s
					}), s
				},
				_space: function() {
					var e = [],
						t = this;
					return f(u, function(i, s) {
						t[s.cache] && e.push(i)
					}), e.pop()
				},
				transition: function(e, t) {
					var s = l(e),
						a = s._space(),
						n = u[a],
						o = 0 === this.alpha() ? l("transparent") : this,
						r = o[n.cache] || n.to(o._rgba),
						h = r.slice();
					return s = s[n.cache], f(n.props, function(e, a) {
						var n = a.idx,
							o = r[n],
							l = s[n],
							u = d[a.type] || {};
						null !== l && (null === o ? h[n] = l : (u.mod && (l - o > u.mod / 2 ? o += u.mod : o - l > u.mod / 2 && (o -= u.mod)), h[n] = i((l - o) * t + o, a)))
					}), this[a](h)
				},
				blend: function(t) {
					if(1 === this._rgba[3]) return this;
					var i = this._rgba.slice(),
						s = i.pop(),
						a = l(t)._rgba;
					return l(e.map(i, function(e, t) {
						return(1 - s) * a[t] + s * e
					}))
				},
				toRgbaString: function() {
					var t = "rgba(",
						i = e.map(this._rgba, function(e, t) {
							return null == e ? t > 2 ? 1 : 0 : e
						});
					return 1 === i[3] && (i.pop(), t = "rgb("), t + i.join() + ")"
				},
				toHslaString: function() {
					var t = "hsla(",
						i = e.map(this.hsla(), function(e, t) {
							return null == e && (e = t > 2 ? 1 : 0), t && 3 > t && (e = Math.round(100 * e) + "%"), e
						});
					return 1 === i[3] && (i.pop(), t = "hsl("), t + i.join() + ")"
				},
				toHexString: function(t) {
					var i = this._rgba.slice(),
						s = i.pop();
					return t && i.push(~~(255 * s)), "#" + e.map(i, function(e) {
						return e = (e || 0).toString(16), 1 === e.length ? "0" + e : e
					}).join("")
				},
				toString: function() {
					return 0 === this._rgba[3] ? "transparent" : this.toRgbaString()
				}
			}), l.fn.parse.prototype = l.fn, u.hsla.to = function(e) {
				if(null == e[0] || null == e[1] || null == e[2]) return [null, null, null, e[3]];
				var t, i, s = e[0] / 255,
					a = e[1] / 255,
					n = e[2] / 255,
					o = e[3],
					r = Math.max(s, a, n),
					h = Math.min(s, a, n),
					l = r - h,
					u = r + h,
					d = .5 * u;
				return t = h === r ? 0 : s === r ? 60 * (a - n) / l + 360 : a === r ? 60 * (n - s) / l + 120 : 60 * (s - a) / l + 240, i = 0 === l ? 0 : .5 >= d ? l / u : l / (2 - u), [Math.round(t) % 360, i, d, null == o ? 1 : o]
			}, u.hsla.from = function(e) {
				if(null == e[0] || null == e[1] || null == e[2]) return [null, null, null, e[3]];
				var t = e[0] / 360,
					i = e[1],
					s = e[2],
					n = e[3],
					o = .5 >= s ? s * (1 + i) : s + i - s * i,
					r = 2 * s - o;
				return [Math.round(255 * a(r, o, t + 1 / 3)), Math.round(255 * a(r, o, t)), Math.round(255 * a(r, o, t - 1 / 3)), n]
			}, f(u, function(s, a) {
				var n = a.props,
					o = a.cache,
					h = a.to,
					u = a.from;
				l.fn[s] = function(s) {
					if(h && !this[o] && (this[o] = h(this._rgba)), s === t) return this[o].slice();
					var a, r = e.type(s),
						d = "array" === r || "object" === r ? s : arguments,
						c = this[o].slice();
					return f(n, function(e, t) {
						var s = d["object" === r ? e : t.idx];
						null == s && (s = c[t.idx]), c[t.idx] = i(s, t)
					}), u ? (a = l(u(c)), a[o] = c, a) : l(c)
				}, f(n, function(t, i) {
					l.fn[t] || (l.fn[t] = function(a) {
						var n, o = e.type(a),
							h = "alpha" === t ? this._hsla ? "hsla" : "rgba" : s,
							l = this[h](),
							u = l[i.idx];
						return "undefined" === o ? u : ("function" === o && (a = a.call(this, u), o = e.type(a)), null == a && i.empty ? this : ("string" === o && (n = r.exec(a), n && (a = u + parseFloat(n[2]) * ("+" === n[1] ? 1 : -1))), l[i.idx] = a, this[h](l)))
					})
				})
			}), l.hook = function(t) {
				var i = t.split(" ");
				f(i, function(t, i) {
					e.cssHooks[i] = {
						set: function(t, a) {
							var n, o, r = "";
							if("transparent" !== a && ("string" !== e.type(a) || (n = s(a)))) {
								if(a = l(n || a), !c.rgba && 1 !== a._rgba[3]) {
									for(o = "backgroundColor" === i ? t.parentNode : t;
										("" === r || "transparent" === r) && o && o.style;) try {
										r = e.css(o, "backgroundColor"), o = o.parentNode
									} catch(h) {}
									a = a.blend(r && "transparent" !== r ? r : "_default")
								}
								a = a.toRgbaString()
							}
							try {
								t.style[i] = a
							} catch(h) {}
						}
					}, e.fx.step[i] = function(t) {
						t.colorInit || (t.start = l(t.elem, i), t.end = l(t.end), t.colorInit = !0), e.cssHooks[i].set(t.elem, t.start.transition(t.end, t.pos))
					}
				})
			}, l.hook(o), e.cssHooks.borderColor = {
				expand: function(e) {
					var t = {};
					return f(["Top", "Right", "Bottom", "Left"], function(i, s) {
						t["border" + s + "Color"] = e
					}), t
				}
			}, n = e.Color.names = {
				aqua: "#00ffff",
				black: "#000000",
				blue: "#0000ff",
				fuchsia: "#ff00ff",
				gray: "#808080",
				green: "#008000",
				lime: "#00ff00",
				maroon: "#800000",
				navy: "#000080",
				olive: "#808000",
				purple: "#800080",
				red: "#ff0000",
				silver: "#c0c0c0",
				teal: "#008080",
				white: "#ffffff",
				yellow: "#ffff00",
				transparent: [null, null, null, 0],
				_default: "#ffffff"
			}
		}(jQuery),
		function() {
			function t(t) {
				var i, s, a = t.ownerDocument.defaultView ? t.ownerDocument.defaultView.getComputedStyle(t, null) : t.currentStyle,
					n = {};
				if(a && a.length && a[0] && a[a[0]])
					for(s = a.length; s--;) i = a[s], "string" == typeof a[i] && (n[e.camelCase(i)] = a[i]);
				else
					for(i in a) "string" == typeof a[i] && (n[i] = a[i]);
				return n
			}

			function i(t, i) {
				var s, n, o = {};
				for(s in i) n = i[s], t[s] !== n && (a[s] || (e.fx.step[s] || !isNaN(parseFloat(n))) && (o[s] = n));
				return o
			}
			var s = ["add", "remove", "toggle"],
				a = {
					border: 1,
					borderBottom: 1,
					borderColor: 1,
					borderLeft: 1,
					borderRight: 1,
					borderTop: 1,
					borderWidth: 1,
					margin: 1,
					padding: 1
				};
			e.each(["borderLeftStyle", "borderRightStyle", "borderBottomStyle", "borderTopStyle"], function(t, i) {
				e.fx.step[i] = function(e) {
					("none" !== e.end && !e.setAttr || 1 === e.pos && !e.setAttr) && (jQuery.style(e.elem, i, e.end), e.setAttr = !0)
				}
			}), e.fn.addBack || (e.fn.addBack = function(e) {
				return this.add(null == e ? this.prevObject : this.prevObject.filter(e))
			}), e.effects.animateClass = function(a, n, o, r) {
				var h = e.speed(n, o, r);
				return this.queue(function() {
					var n, o = e(this),
						r = o.attr("class") || "",
						l = h.children ? o.find("*").addBack() : o;
					l = l.map(function() {
						var i = e(this);
						return {
							el: i,
							start: t(this)
						}
					}), n = function() {
						e.each(s, function(e, t) {
							a[t] && o[t + "Class"](a[t])
						})
					}, n(), l = l.map(function() {
						return this.end = t(this.el[0]), this.diff = i(this.start, this.end), this
					}), o.attr("class", r), l = l.map(function() {
						var t = this,
							i = e.Deferred(),
							s = e.extend({}, h, {
								queue: !1,
								complete: function() {
									i.resolve(t)
								}
							});
						return this.el.animate(this.diff, s), i.promise()
					}), e.when.apply(e, l.get()).done(function() {
						n(), e.each(arguments, function() {
							var t = this.el;
							e.each(this.diff, function(e) {
								t.css(e, "")
							})
						}), h.complete.call(o[0])
					})
				})
			}, e.fn.extend({
				addClass: function(t) {
					return function(i, s, a, n) {
						return s ? e.effects.animateClass.call(this, {
							add: i
						}, s, a, n) : t.apply(this, arguments)
					}
				}(e.fn.addClass),
				removeClass: function(t) {
					return function(i, s, a, n) {
						return arguments.length > 1 ? e.effects.animateClass.call(this, {
							remove: i
						}, s, a, n) : t.apply(this, arguments)
					}
				}(e.fn.removeClass),
				toggleClass: function(t) {
					return function(i, s, a, n, o) {
						return "boolean" == typeof s || void 0 === s ? a ? e.effects.animateClass.call(this, s ? {
							add: i
						} : {
							remove: i
						}, a, n, o) : t.apply(this, arguments) : e.effects.animateClass.call(this, {
							toggle: i
						}, s, a, n)
					}
				}(e.fn.toggleClass),
				switchClass: function(t, i, s, a, n) {
					return e.effects.animateClass.call(this, {
						add: i,
						remove: t
					}, s, a, n)
				}
			})
		}(),
		function() {
			function t(t, i, s, a) {
				return e.isPlainObject(t) && (i = t, t = t.effect), t = {
					effect: t
				}, null == i && (i = {}), e.isFunction(i) && (a = i, s = null, i = {}), ("number" == typeof i || e.fx.speeds[i]) && (a = s, s = i, i = {}), e.isFunction(s) && (a = s, s = null), i && e.extend(t, i), s = s || i.duration, t.duration = e.fx.off ? 0 : "number" == typeof s ? s : s in e.fx.speeds ? e.fx.speeds[s] : e.fx.speeds._default, t.complete = a || i.complete, t
			}

			function i(t) {
				return !t || "number" == typeof t || e.fx.speeds[t] ? !0 : "string" != typeof t || e.effects.effect[t] ? e.isFunction(t) ? !0 : "object" != typeof t || t.effect ? !1 : !0 : !0
			}
			e.extend(e.effects, {
				version: "1.11.0",
				save: function(e, t) {
					for(var i = 0; t.length > i; i++) null !== t[i] && e.data(s + t[i], e[0].style[t[i]])
				},
				restore: function(e, t) {
					var i, a;
					for(a = 0; t.length > a; a++) null !== t[a] && (i = e.data(s + t[a]), void 0 === i && (i = ""), e.css(t[a], i))
				},
				setMode: function(e, t) {
					return "toggle" === t && (t = e.is(":hidden") ? "show" : "hide"), t
				},
				getBaseline: function(e, t) {
					var i, s;
					switch(e[0]) {
						case "top":
							i = 0;
							break;
						case "middle":
							i = .5;
							break;
						case "bottom":
							i = 1;
							break;
						default:
							i = e[0] / t.height
					}
					switch(e[1]) {
						case "left":
							s = 0;
							break;
						case "center":
							s = .5;
							break;
						case "right":
							s = 1;
							break;
						default:
							s = e[1] / t.width
					}
					return {
						x: s,
						y: i
					}
				},
				createWrapper: function(t) {
					if(t.parent().is(".ui-effects-wrapper")) return t.parent();
					var i = {
							width: t.outerWidth(!0),
							height: t.outerHeight(!0),
							"float": t.css("float")
						},
						s = e("<div></div>").addClass("ui-effects-wrapper").css({
							fontSize: "100%",
							background: "transparent",
							border: "none",
							margin: 0,
							padding: 0
						}),
						a = {
							width: t.width(),
							height: t.height()
						},
						n = document.activeElement;
					try {
						n.id
					} catch(o) {
						n = document.body
					}
					return t.wrap(s), (t[0] === n || e.contains(t[0], n)) && e(n).focus(), s = t.parent(), "static" === t.css("position") ? (s.css({
						position: "relative"
					}), t.css({
						position: "relative"
					})) : (e.extend(i, {
						position: t.css("position"),
						zIndex: t.css("z-index")
					}), e.each(["top", "left", "bottom", "right"], function(e, s) {
						i[s] = t.css(s), isNaN(parseInt(i[s], 10)) && (i[s] = "auto")
					}), t.css({
						position: "relative",
						top: 0,
						left: 0,
						right: "auto",
						bottom: "auto"
					})), t.css(a), s.css(i).show()
				},
				removeWrapper: function(t) {
					var i = document.activeElement;
					return t.parent().is(".ui-effects-wrapper") && (t.parent().replaceWith(t), (t[0] === i || e.contains(t[0], i)) && e(i).focus()), t
				},
				setTransition: function(t, i, s, a) {
					return a = a || {}, e.each(i, function(e, i) {
						var n = t.cssUnit(i);
						n[0] > 0 && (a[i] = n[0] * s + n[1])
					}), a
				}
			}), e.fn.extend({
				effect: function() {
					function i(t) {
						function i() {
							e.isFunction(n) && n.call(a[0]), e.isFunction(t) && t()
						}
						var a = e(this),
							n = s.complete,
							r = s.mode;
						(a.is(":hidden") ? "hide" === r : "show" === r) ? (a[r](), i()) : o.call(a[0], s, i)
					}
					var s = t.apply(this, arguments),
						a = s.mode,
						n = s.queue,
						o = e.effects.effect[s.effect];
					return e.fx.off || !o ? a ? this[a](s.duration, s.complete) : this.each(function() {
						s.complete && s.complete.call(this)
					}) : n === !1 ? this.each(i) : this.queue(n || "fx", i)
				},
				show: function(e) {
					return function(s) {
						if(i(s)) return e.apply(this, arguments);
						var a = t.apply(this, arguments);
						return a.mode = "show", this.effect.call(this, a)
					}
				}(e.fn.show),
				hide: function(e) {
					return function(s) {
						if(i(s)) return e.apply(this, arguments);
						var a = t.apply(this, arguments);
						return a.mode = "hide", this.effect.call(this, a)
					}
				}(e.fn.hide),
				toggle: function(e) {
					return function(s) {
						if(i(s) || "boolean" == typeof s) return e.apply(this, arguments);
						var a = t.apply(this, arguments);
						return a.mode = "toggle", this.effect.call(this, a)
					}
				}(e.fn.toggle),
				cssUnit: function(t) {
					var i = this.css(t),
						s = [];
					return e.each(["em", "px", "%", "pt"], function(e, t) {
						i.indexOf(t) > 0 && (s = [parseFloat(i), t])
					}), s
				}
			})
		}(),
		function() {
			var t = {};
			e.each(["Quad", "Cubic", "Quart", "Quint", "Expo"], function(e, i) {
				t[i] = function(t) {
					return Math.pow(t, e + 2)
				}
			}), e.extend(t, {
				Sine: function(e) {
					return 1 - Math.cos(e * Math.PI / 2)
				},
				Circ: function(e) {
					return 1 - Math.sqrt(1 - e * e)
				},
				Elastic: function(e) {
					return 0 === e || 1 === e ? e : -Math.pow(2, 8 * (e - 1)) * Math.sin((80 * (e - 1) - 7.5) * Math.PI / 15)
				},
				Back: function(e) {
					return e * e * (3 * e - 2)
				},
				Bounce: function(e) {
					for(var t, i = 4;
						((t = Math.pow(2, --i)) - 1) / 11 > e;);
					return 1 / Math.pow(4, 3 - i) - 7.5625 * Math.pow((3 * t - 2) / 22 - e, 2)
				}
			}), e.each(t, function(t, i) {
				e.easing["easeIn" + t] = i, e.easing["easeOut" + t] = function(e) {
					return 1 - i(1 - e)
				}, e.easing["easeInOut" + t] = function(e) {
					return .5 > e ? i(2 * e) / 2 : 1 - i(-2 * e + 2) / 2
				}
			})
		}(), e.effects
});