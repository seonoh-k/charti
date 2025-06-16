function execDaumPostcode() {
    new daum.Postcode({
        oncomplete: function(data) {
            // 1) 시도, 구군
            const sido    = data.sido;      // ex. "서울특별시" :contentReference[oaicite:0]{index=0}
            const sigungu = data.sigungu;   // ex. "강남구" :contentReference[oaicite:1]{index=1}

            // 2) 지번주소 → 동, 번지 분리
            //    data.jibunAddress 예: "서울특별시 강남구 역삼동 123-45"
            const jibun = data.jibunAddress.replace(`${sido} ${sigungu} `, "");
            // jibun === "역삼동 123-45"
            const [dong, ...beonjiParts] = jibun.split(" ");
            const beonji = beonjiParts.join(" ");  // "123-45"

            // 3) 각 필드에 값 채우기
            document.getElementById('zip_num').value = data.zonecode;
            document.getElementById("sido").value    = sido;
            document.getElementById("gugun").value = sigungu;
            document.getElementById("dong").value     = dong;
            document.getElementById("beonji").value   = beonji;
        }
    }).open();
}