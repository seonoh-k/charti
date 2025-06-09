// 스토리지를 이용한 파일 업로드 / 다운로드 / 이미지 출력 관련 함수
// 테스트에 사용한 함수 그대로 옮겼기 때문에 함수 수정 필요

// 파일 업로드 함수
async function handleUploadFile(e) {
    e.preventDefault();

    // 예시 - <input type="file">
    const fileInput = document.querySelector("#fileInput");
    // 업로드할 파일
    const file = fileInput.files[0];
    // 결과를 담을 div 태그
    const result = document.querySelector("#upload-result");

    // 업로드할 파일이 없으면 경고 출력
    if(!file) {
        alert("select file!");
        return
    }

    // 업로드 url 생성 요청
    const res = await fetch("/api/file/upload" , {
        method: "POST",
        body: new FormData(e.target)
    })

    // 반환된 UrlResponse
    const data = await res.json();

    // 반환 받은 UrlResponse에 url이 없는 경우 문구 출력 -> url 생성 실패
    if (!data.url) {
        result.innerHTML = `<p class="text-red-600 font-semibold">서버로부터 업로드 URL을 받지 못했습니다.</p>`;
        return;
    }

    // 인증된 파일 업로드 url
    const uploadUrl = data.url;

    // 생성된 업로드 url을 통해 파일 업로드 요청
    const uploadRes = await fetch(uploadUrl, {
        method: "PUT",
        headers: {
            "Content-Type": file.type
        },
        body: file
    });

    // 업로드 결과에 따라 결과 문구 출력
    if(uploadRes.ok) {
        result.innerHTML = `<p class="text-green-600 font-semibold">Upload Seccess</p>`;
    }else {
        result.innerHTML = `<p class="text-red-600 font-semibold">Upload Fail!</p>`;
    }
}

// 파일 다운로드 함수
async function handleDownloadFile(e) {
    e.preventDefault();

    // 다운로드할 파일명
    const key = document.querySelector("#filenameInput").value.trim();
    // 결과를 출력할 div 태그
    const result = document.querySelector("#download-result");

    // 파일명 누락시 경고 출력
    if(!key) {
        alert("Need Filename");
        return
    }

    // API url 요청을 위해 파일명을 인코딩 - url에서 사용하는 기호와 백엔드에서 사용하는 기호가 달라질 수 있기 때문
    const encoded = encodeURIComponent(key);
    // 다운로드 url 생성 요청
    const res = await fetch(`/api/file/download/${encoded}`);
    // 응답 실패 시 경고 출력
    if(!res.ok) {
        alert("Server Error");
        return
    }

    // 반환된 UrlResponse
    const data = await res.json();

    // 반환 받은 UrlResponse에 url이 없는 경우 문구 출력 -> url 생성 실패
    if(!downloadUrl){
        result.innerHTML = `<p class="text-red-600 font-semibold">URL Not Found</p>`;
        return
    }

    // 인증된 파일 다운로드 url
    const downloadUrl = data.url;
    // 다운로드를 실행하기 위한 a 태그 생성
    const a = document.createElement("a");
    // 링크에 반환 받은 url 입력
    a.href = downloadUrl;
    // body 안에 생성한 a 태그 추가
    document.body.appendChild(a);
    // a 태그를 클릭하게 함 -> 다운로드가 진행됨
    a.click();
    // a 태그 삭제
    a.remove();

    // 다운로드 결과 출력
    result.innerHTML = `<p class="text-green-600 font-semibold">Download Success</p>`;
}

// 이미지 출력 함수
async function handlePrintImage(e) {
    e.preventDefault();

    // 출력할 파일명
    const key = document.querySelector("#filenameInput").value.trim();
    // 결과를 출력할 div 태그
    const result = document.querySelector("#download-result");

    // 파일명 누락시 경고 출력
    if(!key) {
        alert("Need Filename");
        return
    }

    // API url 요청을 위해 파일명을 인코딩
    const encoded = encodeURIComponent(key);
    // 이미지 출력용 url 생성 요청
    const res = await fetch(`/api/file/show/${encoded}`);
    // 응답 실패 시 경고 출력
    if(!res.ok) {
        alert("Server Error");
        return
    }

    // 반환된 UrlResponse
    const data = await res.json();
    // 반환 받은 UrlResponse에 url이 없는 경우 문구 출력 -> url 생성 실패
    if(!imageUrl){
        result.innerHTML = `<p class="text-red-600 font-semibold">URL Not Found</p>`;
        return
    }
    // 인증된 파일 다운로드 url
    const imageUrl = data.url;

    // 이미지 삽입 전 태그 내용 비우기
    result.innerHTML = ``;
    // 결과 문구와 이미지 삽입
    result.innerHTML = `
			<p class="text-green-600 font-semibold">File Load Success</p>
			<img src="${imageUrl}" alt="${data.filename}" class="mt-4 rounded-lg shadow-md w-full" />
			`;
}