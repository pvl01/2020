
// Creates the setObj method for arrays
Storage.prototype.setObj = function(key, obj) {
    return this.setItem(key, JSON.stringify(obj))
}

// Creates the getObj method for arrays
Storage.prototype.getObj = function(key) {
    return JSON.parse(this.getItem(key))
}

if (localStorage.getObj("FIVE") == null) {
	localStorage.setObj("FIVE", ["This is the sentence evolution"]);
}

if (localStorage.getObj("FOUR") == null) {
	localStorage.setObj("FOUR", ["Hack the Heights Four"]);
}

if (localStorage.getObj("THREE") == null) {
	localStorage.setObj("THREE", ["Hack the Heights"]);
}

if (localStorage.getObj("TWO") == null) {
	localStorage.setObj("TWO", ["Boston College"]);
}

if (localStorage.getObj("ONE") == null) {
	localStorage.setObj("ONE", ["Northeastern"]);
}

var five_words = localStorage.getObj("FIVE");
var four_words = localStorage.getObj("FOUR");
var three_words = localStorage.getObj("THREE");
var two_words = localStorage.getObj("TWO");
var one_words = localStorage.getObj("ONE");

function capitalize(w) {
  return w.charAt(0).toUpperCase() + w.substring(1);
}

/* Updates the newest element of sentences of the given size */
function update(size, event) {
	event.preventDefault();
	var new_word = document.getElementById("new_word").value;
	var old_word = document.getElementById("old_word").value;
	if (size == 1) {
		var new_words = one_words[one_words.length - 1].replace(old_word, new_word);
		one_words.push(new_words);
		localStorage.setObj("ONE", one_words);
           one_words[one_words.length - 1] = capitalize(one_words[one_words.length - 1]);
		document.getElementById("w1").innerHTML = one_words[one_words.length - 1];
	}
	else if (size == 2) {
		var new_words = two_words[two_words.length - 1].replace(old_word, new_word);
		two_words.push(new_words);
		localStorage.setObj("TWO", two_words);
            two_words[two_words.length - 1] = capitalize(two_words[two_words.length - 1]);
		document.getElementById("w2").innerHTML = two_words[two_words.length - 1];
	}
	else if (size == 3) {
		var new_words = three_words[three_words.length - 1].replace(old_word, new_word);
		three_words.push(new_words);
		localStorage.setObj("THREE", three_words);
            three_words[three_words.length - 1] = capitalize(three_words[three_words.length - 1]);
		document.getElementById("w3").innerHTML = three_words[three_words.length - 1];
	}
	else if (size == 4) {
		var new_words = four_words[four_words.length - 1].replace(old_word, new_word);
		four_words.push(new_words);
		localStorage.setObj("FOUR", four_words);
            four_words[four_words.length - 1] = capitalize(four_words[four_words.length - 1]);
		document.getElementById("w4").innerHTML = four_words[four_words.length - 1];
	}
	else if (size == 5) {
		var new_words = five_words[five_words.length - 1].replace(old_word, new_word);
		five_words.push(new_words);
		localStorage.setObj("FIVE", five_words);
            five_words[five_words.length - 1] = capitalize(five_words[five_words.length - 1]);
		document.getElementById("w5").innerHTML = five_words[five_words.length - 1];
	}
}

function listAll(size, event) {
	event.preventDefault();
	text = "<ol>";
	if (size == 1) {
		for (i = 0; i < one_words.length; i++) {
			text += "<li>" + one_words[i] + "</li>";
		}
	}
	else if (size == 2) {
		for (i = 0; i < two_words.length; i++) {
			text += "<li>" + two_words[i] + "</li>";
		}
	}
	else if (size == 3) {
		for (i = 0; i < three_words.length; i++) {
			text += "<li>" + three_words[i] + "</li>";
		}
	}
	else if (size == 4) {
		for (i = 0; i < four_words.length; i++) {
			text += "<li>" + four_words[i] + "</li>";
		}
	}
	else if (size == 5) {
		for (i = 0; i < five_words.length; i++) {
			text += "<li>" + five_words[i] + "</li>";
		}
	}
	text += "</ol>";
	document.getElementById("list").innerHTML = text;
}


