
// This file is intentionally blank
// Use this file to add JavaScript to your project
//using materializecss..

const switchers = [...document.querySelectorAll('.switcher')]

switchers.forEach(item => {
	item.addEventListener('click', function() {
		switchers.forEach(item => item.parentElement.classList.toggle('is-active'))
		//		this.parentElement.classList.add('is-active')
	})
})

window.onload = function() {
	document.getElementById('icon-image').addEventListener('change', (event) => {
		const file = event.target.files[0]
		if (!file) return
		const reader = new FileReader()
		reader.onload = (event) => {
			document.querySelector('#iconimg').src = event.target.result
			document.querySelector('#iconimg').style.border = "2px solid #cddbef"
		}
		reader.readAsDataURL(file)

		document.getElementById('unset').innerText = ""
	})
};



//const logout = () => {
//	const value = sessionStorage.getItem('login');
//	if (value == 1) {
//		Swal.fire({
//			type: "question",
//			title: "ログアウト",
//			text: "ログアウトしますか？",
//			confirmButtonColor: '#3CB371',
//			showCancelButton: true
//		}).then((result) => {
//			if (result.isConfirmed) {
//				sessionStorage.removeItem('id');
//				window.location.href = 'login'
//			}
//
//		});
//	} else {
//		Swal.fire({
//			type: "info",
//			title: "ログアウト",
//			text: "ログインしてください",
//			confirmButtonColor: '#3CB371',
//		}).then((result) => {
//			if (result.isConfirmed) {
//				window.location.href = 'login';
//			}
//
//		});
//	}
//
//}
//
//const login = () => {
//	const value = sessionStorage.getItem('login');
//	if(value == 1) {
//		Swal.fire({
//			type: "info",
//			title: "ログイン",
//			text: "ログイン中です",
//			confirmButtonColor: '#3CB371',
//		})
//	} else {
//		window.location.href = 'login';
//	}
//}
//
