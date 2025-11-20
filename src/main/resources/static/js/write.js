// Quill 이미지 리사이즈 모듈 등록
const imageResizeModule = window.ImageResize?.default || window.ImageResize;
Quill.register("modules/imageResize", imageResizeModule);

const toolbarOptions = [
    [{ header: [1, 2, 3, false] }],
    ["bold", "italic", "underline", "strike"],
    [{ align: [] }],
    [{ list: "ordered" }, { list: "bullet" }],
    ["image", "video"],
    ["clean"],
];

// 전역 변수로 선언하여 HTML 내 스크립트에서 접근 가능하게 함
var quill = new Quill("#editor", {
    theme: "snow",
    placeholder: "당신의 이야기를 POST-IT!",
    modules: {
        toolbar: {
            container: toolbarOptions,
            handlers: {
                image: imageHandler,
                video: function () {
                    const inputUrl = prompt("링크를 입력해 주세요.");
                    if (!inputUrl) return;
                    let url = inputUrl.trim();
                    let videoId = null;
                    try {
                        const parsed = new URL(url);
                        const host = parsed.hostname.replace("www.", "");
                        if (host.includes("youtube.com") || host.includes("youtu.be")) {
                            if (parsed.searchParams.get("v")) {
                                videoId = parsed.searchParams.get("v");
                            } else if (host === "youtu.be") {
                                videoId = parsed.pathname.split("/")[1];
                            } else if (parsed.pathname.startsWith("/shorts/")) {
                                videoId = parsed.pathname.split("/")[2];
                            }
                        }
                    } catch (e) { console.warn("잘못된 URL:", e); }

                    if (videoId) {
                        const embedUrl = `https://www.youtube.com/embed/${videoId}`;
                        const range = this.quill.getSelection(true);
                        this.quill.insertEmbed(range.index, "video", embedUrl);
                    } else { alert("유효한 링크가 아닙니다!"); }
                },
            },
        },
        imageResize: { modules: ["Resize", "DisplaySize", "Toolbar"] },
    },
});

function imageHandler() {
    const input = document.createElement('input');
    input.setAttribute('type', 'file');
    input.setAttribute('accept', 'image/*');
    input.click();
    input.onchange = async () => {
        const file = input.files[0];
        if (!file) return;
        const formData = new FormData();
        formData.append('file', file);
        try {
            const response = await fetch('/blog/upload/image', { method: 'POST', body: formData });
            if (response.ok) {
                const imageUrl = await response.text();
                const range = quill.getSelection(true);
                quill.insertEmbed(range.index, 'image', imageUrl);
                quill.setSelection(range.index + 1);
            } else { alert('이미지 업로드 실패'); }
        } catch (error) { console.error('Error:', error); alert('오류 발생'); }
    };
}

/**
 * 게시글 작성/수정 버튼 클릭 이벤트
 */
document.getElementById('postButton').addEventListener('click', function () {
    const form = document.getElementById('postForm');
    const blogId = form.getAttribute('data-blog-id');

    // [핵심] HTML에서 설정한 mode 값을 읽어옴
    const mode = form.getAttribute('data-mode'); // 'write' or 'edit'
    const postId = form.getAttribute('data-post-id'); // 수정 시 사용

    const categoryId = document.getElementById('categorySelect').value;
    const title = document.getElementById('titleInput').value;
    const content = quill.root.innerHTML;

    if (!categoryId) { alert('카테고리를 선택해주세요.'); return; }
    if (!title.trim()) { alert('제목을 입력해주세요.'); return; }
    if (quill.getText().trim().length === 0 && content.indexOf('<img') === -1) {
        alert('내용을 입력해주세요.');
        return;
    }

    const postData = {
        categoryId: categoryId,
        title: title,
        content: content
    };

    // [핵심] 모드에 따라 요청 URL 분기
    let url = `/blog/${blogId}/write`;
    if (mode === 'edit') {
        url = `/blog/${blogId}/post/${postId}/edit`;
    }

    fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(postData)
    })
    .then(response => {
        if (response.ok) {
            alert(mode === 'edit' ? '게시글이 수정되었습니다.' : '게시글이 등록되었습니다.');
            // 수정 후엔 상세 페이지로, 작성 후엔 메인으로
            if (mode === 'edit') {
                location.href = `/blog/${blogId}/post/${postId}`;
            } else {
                location.href = `/blog/${blogId}`;
            }
        } else {
            return response.text().then(text => { throw new Error(text) });
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('실패: ' + error.message);
    });
});