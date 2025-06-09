// 파일 업로드 함수
async function handleUploadFile(e) {
    e.preventDefault();
    const fileInput = document.querySelector("#fileInput");
    const file = fileInput.files[0];
    const result = document.querySelector("#upload-result");

    if(!file) {
        alert("select file!");
        return
    }

    const res = await fetch("/api/file/upload" , {
        method: "POST",
        body: new FormData(e.target)
    })

    const data = await res.json();

    if (!data.url) {
        result.innerHTML = `<p class="text-red-600 font-semibold">서버로부터 업로드 URL을 받지 못했습니다.</p>`;
        return;
    }


    const uploadUrl = data.url;

    const uploadRes = await fetch(uploadUrl, {
        method: "PUT",
        headers: {
            "Content-Type": file.type
        },
        body: file
    });


    if(uploadRes.ok) {
        result.innerHTML = `<p class="text-green-600 font-semibold">Upload Seccess</p>`;
    }else {
        result.innerHTML = `<p class="text-red-600 font-semibold">Upload Fail!</p>`;
    }
}

// 파일 다운로드 함수
async function handleDownloadFile(e) {
    e.preventDefault();

    const key = document.querySelector("#filenameInput").value.trim();
    const result = document.querySelector("#download-result");

    if(!key) {
        alert("Need Filename");
        return
    }

    const encoded = encodeURIComponent(key);
    const res = await fetch(`/api/file/download/${encoded}`);
    if(!res.ok) {
        alert("Server Error");
        return
    }

    const data = await res.json();
    const downloadUrl = data.url;
    if(!downloadUrl){
        result.innerHTML = `<p class="text-red-600 font-semibold">URL Not Found</p>`;
        return
    }

    const a = document.createElement("a");
    a.href = downloadUrl;
    document.body.appendChild(a);
    a.click();
    a.remove();

    result.innerHTML = `<p class="text-green-600 font-semibold">Download Success</p>`;
}

// 이미지 출력 함수
async function handlePrintImage(e) {
    e.preventDefault();

    const key = document.querySelector("#filenameInput").value.trim();
    const result = document.querySelector("#download-result");

    if(!key) {
        alert("Need Filename");
        return
    }

    const encoded = encodeURIComponent(key);
    const res = await fetch(`/api/file/show/${encoded}`);
    if(!res.ok) {
        alert("Server Error");
        return
    }

    const data = await res.json();
    const imageUrl = data.url;
    if(!imageUrl){
        result.innerHTML = `<p class="text-red-600 font-semibold">URL Not Found</p>`;
        return
    }

    result.innerHTML = ``;

    result.innerHTML = `
			<p class="text-green-600 font-semibold">File Load Success</p>
			<img src="${imageUrl}" alt="${data.filename}" class="mt-4 rounded-lg shadow-md w-full" />
			`;
}