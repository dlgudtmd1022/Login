// 삭제 기능 -> 수정
const deleteButton =  document.getElementById('delete-btn');

if (deleteButton) {
    // deleteButton.addEventListener('click', event => {
    //     let id = document.getElementById('article-id').value;
    //     fetch(`/api/articles/${id}`, {
    //         method: 'DELETE'
    //     })
    //         .then(() => {
    //             alert('삭제가 완료되었습니다.');
    //             location.replace('/articles');
    //         })
    // })
    deleteButton.addEventListener("click", (event) => {
        let id = document.getElementById("article-id").value;
        function success(){
            alert("삭제가 완료되었습니다.");
            location.replace("/articles");
        }

        function fail(){
            alert("삭제 실패했습니다.");
            location.replace("/articles");
        }

        httpRequest("DELETE", "/api/articles/"+ id, null, success, fail);
    });
}

// 수정 기능 -> 수정
// id가 modify-btn인 엘리먼트 조회
const modifyButton = document.getElementById('modify-btn');

if(modifyButton) {
    // // 클릭 이벤트가 감지되면 수정 API 요청
    // modifyButton.addEventListener('click', event => {
    //     let params = new URLSearchParams(location.search);
    //     let id = params.get('id');
    //
    //     fetch(`/api/articles/${id}`, {
    //         method: 'PUT',
    //         headers: {
    //             "Content-Type": "application/json",
    //         },
    //         body: JSON.stringify({
    //             title: document.getElementById('title').value,
    //             content: document.getElementById('content').value
    //         })
    //     })
    //         .then(() => {
    //             alert('수정이 완료되었습니다.');
    //             location.replace(`/articles/${id}`);
    //         })
    // })
    modifyButton.addEventListener("click", (event) => {
        let params = new URLSearchParams(location.search);
        let id = params.get("id");

        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value,
        });

        function success(){
            alert("수정 완료되었습니다.");
            location.replace("/articles/" + id);
        }

        function fail(){
            alert("수정 실패했습니다.");
            location.replace("/articles/" + id);
        }

        httpRequest("PUT", "/api/articles/" + id, body, success, fail);
    });
}

// 등록 기능
// id가 create-btn인 엘리먼트
const createButton = document.getElementById("create-btn");

if(createButton){
    // 클릭 이벤트가 감지되면 생성 API 요청 -> 수정
    // createButton.addEventListener("click", (event) => {
    //     fetch("/api/articles", {
    //         method:"POST",
    //         headers: {
    //         "Content-Type" : "application/json",
    //     },
    //     body: JSON.stringify({
    //         title: document.getElementById("title").value,
    //         content: document.getElementById("content").value,
    //     }),
    // }).then(() => {
    //     alert("등록 완료되었습니다..");
    //     location.replace("/articles");
    //     });
    // });
    // 등록 버튼을 클릭하면 /api/articles로 요청 보냄
    createButton.addEventListener("click", (event) =>{
        body = JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value,
        });
        function success(){
            alert("등록 완료되었습니다.");
            location.replace("/articles");
        }
        function fail(){
            alert("등록 실패했습니다.");
            location.replace("/articles");
        }

        httpRequest("POST", "/api/articlees", body, success, fail);
    });
}

// 쿠키를 가져오는 함수
function getCookie(key){
    var result = null;
    var cookie = document.cookie.split(";");
    cookie.some(function (item){
        item = item.replace(" ", "");

        var dic = item.split("=");

        if(key == div[0]){
            result = dic[1];
            return true;
        }
    });
    return result;
}

// HTTP 요청을 보내는 함수
function httpRequest(method, url, body, success, fail){
    fetch(url, {
        method: method,
        headers: {
            // 로컬 스토리지에서 액세스 토큰 값을 가져와 헤더에 추가
            Authorization: "Bearer" + localStorage.getItem("access_token"),
            "Content-Type": "application/json",
        },
        body: body,
    }). then((response) => {
        if (response.status === 200 || response.status === 201){
            return success();
        }
        const refresh_token = getCookie("refresh_token");
        if(response.status === 401 && refresh_token){
            fetch("/api/token", {
                method: "POST",
                headers: {
                    Authorizatioin: "Bearer" + localStorage.getItem("access_token"),
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    refreshToken: getCookie("refresh_token"),
                }),
            })
                .then((res) => {
                    if (res.ok){
                        return res.json();
                    }
                })
                .then((result) => {
                    // 재발급이 성공하면 로컬 스토리지값을 새로운 색세스 토큰으로 교체
                    localStorage.setItem("access_token", result.accessToken);
                    httpRequest(method, url, body, success, fail);
                })
                .catch((error) => fail());
        }else {
            return fail();
        }
    });
}