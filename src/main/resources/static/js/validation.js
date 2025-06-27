// validation.js
(() => {
    const rules = {
        name: {
            regex: /^[가-힣]{2,8}$/,
            message: '2~8자 한글만 가능합니다. 공백은 불가.'
        },
        nickname: {
            regex: /^[가-힣A-Za-z0-9_-]{2,10}$/,
            message: '2~10자: 한글·영문·숫자·_,- 만 가능합니다. 공백 금지.'
        },
        username: {
            regex: /^\S{4,30}$/,
            message: '4~30자, 공백 없이 입력해주세요.'
        },
        password: {
            regex: /^(?=.{8,16}$)(?=.*[a-z])(?=.*[A-Z])(?=.*\W)[^\s]+$/,
            message: '8~16자, 대문자·소문자·특수문자 모두 1개 이상 포함. 공백 금지.'
        },
        newPassword: {
            regex: /^(?=.{8,16}$)(?=.*[a-z])(?=.*[A-Z])(?=.*\W)[^\s]+$/,
            message: '8~16자, 대문자·소문자·특수문자 모두 1개 이상 포함. 공백 금지.'
        }
    };

    // 비정상 한글 음절 리스트 (확장 가능)
    const abnormalSyllables = [
            "팑","팒","팕","팘","팙","팚","팛","팞","팭","팮",
            "팱","팴","팵","팶","팷","팺","퍉","퍊","퍍","퍐",
            "퍑","퍒","퍓","퍖","퍥","퍦","퍩","퍬","퍭","퍮",
            "퍯","퍲","펁","펂","펅","펈","펉","펊","펋","펎",
            "펝","펞","펡","펤","펥","펦","펧","펪","펹","펺",
            "펽","폀","폁","폂","폃","폆","폕","폖","폙","폜",
            "폝","폞","폟","폢","폱","폲","폵","폸","폹","폺",
            "폻","폾","퐍","퐎","퐑","퐔","퐕","퐖","퐗","퐚",
            "퐩","퐪","퐭","퐰","퐱","퐲","퐳","퐶","푅","푆",
            "푉","푌","푍","푎","푏","푒","푡","푢","푥","푨",
            "푩","푪","푫","푮","푽","푾","풁","풄","풅","풆",
            "풇","풊","풙","풚","풝","풠","풡","풢","풣","풦",
            "풵","풶","풹","풼","풽","풾","풿","퓂","퓑","퓒",
            "퓕","퓘","퓙","퓚","퓛","퓞","퓭","퓮","퓱","퓴",
            "퓵","퓶","퓷","퓺","픉","픊","픍","픐","픑","픒",
            "픓","픖","픥","픦","픩","픬","픭","픮","픯","픲",
            "핁","핂","핅","핈","핉","핊","핋","핎","싽","싾",
            "쌁","쌄","쌅","쌆","쌇","쌊","쌙","쌚","쌝","쌠",
            "쌡","쌢","쌣","쌦","쌵","쌶","쌹","쌼","쌽","쌾",
            "쌿","썂","썑","썒","썕","썘","썙","썚","썛","썞",
            "썭","썮","썱","썴","썵","썶","썷","썺","쎉","쎊",
            "쎍","쎐","쎑","쎒","쎓","쎖","쎥","쎦","쎩","쎬",
            "쎭","쎮","쎯","쎲","쏁","쏂","쏅","쏈","쏉","쏊",
            "쏋","쏎","쏝","쏞","쏡","쏤","쏥","쏦","쏧","쏪",
            "쏹","쏺","쏽","쐀","쐁","쐂","쐃","쐆","쐕","쐖",
            "쐙","쐜","쐝","쐞","쐟","쐢","쐱","쐲","쐵","쐸",
            "쐹","쐺","쐻","쐾","쑍","쑎","쑑","쑔","쑕","쑖",
            "쑗","쑚","쑩","쑪","쑭","쑰","쑱","쑲","쑳","쑶",
            "쒅","쒆","쒉","쒌","쒍","쒎","쒏","쒒","쒡","쒢",
            "쒥","쒨","쒩","쒪","쒫","쒮","쒽","쒾","쓁","쓄"
    ];

    // 특정 문자열에 비정상 음절이 포함되어 있는지 검사
    function containsAbnormalSyllables(value) {
        return abnormalSyllables.some(syllable => value.includes(syllable));
    }

    function attachFieldValidation(el, rule) {
        el.addEventListener('input', () => {
            el.setCustomValidity('');
            const value = el.value;

            if (!rule.regex.test(value)) {
                el.setCustomValidity(rule.message);
            } else if (el.id === 'name' && containsAbnormalSyllables(value)) {
                el.setCustomValidity('이름에 올바르지 않은 음절이 포함되어 있습니다.');
            }
        });

        el.addEventListener('blur', () => {
            const value = el.value.trim();;

            if (!rule.regex.test(value)) {
                el.reportValidity();
            } else if (el.id === 'name' && containsAbnormalSyllables(value)) {
                el.setCustomValidity('이름에 올바르지 않은 음절이 포함되어 있습니다.');
                el.reportValidity();
            }
        });
    }

    // 폼 submit 시 최종 검증
    function attachFormValidation(form) {
        form.addEventListener('submit', e => {
            let allValid = true;
            Object.entries(rules).forEach(([key, rule]) => {
                const el = form.querySelector(`#${key}`);
                if (!el) return;
                el.value = el.value.trim();
                if (!rule.regex.test(el.value)) {
                    el.setCustomValidity(rule.message);
                    el.reportValidity();
                    allValid = false;
                } else {
                    el.setCustomValidity('');
                }
            });
            // if (!allValid) e.preventDefault();
        });
    }

    document.addEventListener('DOMContentLoaded', () => {
        const form = document.getElementById('signupForm');
        if (!form) return;

        // 각 필드에 이벤트 연결
        Object.entries(rules).forEach(([key, rule]) => {
            const el = form.querySelector(`#${key}`);
            if (el) attachFieldValidation(el, rule);
        });

        // 폼 submit 검증
        attachFormValidation(form);
    });
})();
