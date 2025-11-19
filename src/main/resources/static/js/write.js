// Quill 이미지 리사이즈 모듈 등록
const imageResizeModule = window.ImageResize?.default || window.ImageResize;
Quill.register("modules/imageResize", imageResizeModule);

// 툴바 옵션
const toolbarOptions = [
    [{ header: [1, 2, 3, false] }],
    ["bold", "italic", "underline", "strike"],
    [{ align: [] }],
    [{ list: "ordered" }, { list: "bullet" }],
    ["image", "video"],
    ["clean"],
];

// Quill 에디터 초기화
const quill = new Quill("#editor", {
    theme: "snow",
    placeholder: "당신의 이야기를 POST-IT!",
    modules: {
        toolbar: {
            container: toolbarOptions,
            handlers: {
                // 이미지 핸들러 커스텀 (서버 업로드)
                image: imageHandler,
                video: function () {
                    const inputUrl = prompt("링크를 입력해 주세요.");
                    if (!inputUrl) return;

                    let url = inputUrl.trim();
                    let videoId = null;

                    try {
                        const parsed = new URL(url);
                        const host = parsed.hostname.replace("www.", "");

                        // YouTube URL 처리
                        if (host.includes("youtube.com") || host.includes("youtu.be")) {
                            if (parsed.searchParams.get("v")) {
                                videoId = parsed.searchParams.get("v");
                            } else if (host === "youtu.be") {
                                videoId = parsed.pathname.split("/")[1];
                            } else if (parsed.pathname.startsWith("/shorts/")) {
                                videoId = parsed.pathname.split("/")[2];
                            }
                        }
                    } catch (e) {
                        console.warn("잘못된 URL:", e);
                    }

                    if (videoId) {
                        const embedUrl = `https://www.youtube.com/embed/${videoId}`;
                        const range = this.quill.getSelection(true);
                        this.quill.insertEmbed(range.index, "video", embedUrl);
                    } else {
                        alert("유효한 링크가 아닙니다!");
                    }
                },
            },
        },
        imageResize: {
            modules: ["Resize", "DisplaySize", "Toolbar"],
        },
    },
});

/**
 * 이미지 핸들러: 이미지를 선택하면 서버로 비동기 업로드 후 URL을 에디터에 삽입
 */
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
            const response = await fetch('/blog/upload/image', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const imageUrl = await response.text(); // 서버에서 URL 리턴
                const range = quill.getSelection(true);
                // 에디터에 이미지 삽입
                quill.insertEmbed(range.index, 'image', imageUrl);
                // 커서를 이미지 뒤로 이동
                quill.setSelection(range.index + 1);
            } else {
                alert('이미지 업로드 실패');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('이미지 업로드 중 오류가 발생했습니다.');
        }
    };
}

/**
 * 게시글 작성(POST) 버튼 클릭 이벤트
 */
document.getElementById('postButton').addEventListener('click', function () {
    const form = document.getElementById('postForm');
    const blogId = form.getAttribute('data-blog-id');
    const categoryId = document.getElementById('categorySelect').value;
    const title = document.getElementById('titleInput').value;
    const content = quill.root.innerHTML; // HTML 내용 가져오기

    // 유효성 검사
    if (!categoryId) {
        alert('카테고리를 선택해주세요.');
        return;
    }
    if (!title.trim()) {
        alert('제목을 입력해주세요.');
        return;
    }
    if (quill.getText().trim().length === 0 && content.indexOf('<img') === -1) {
        alert('내용을 입력해주세요.');
        return;
    }

    // 데이터 전송 객체 생성
    const postData = {
        categoryId: categoryId,
        title: title,
        content: content
    };

    // AJAX 전송
    fetch(`/blog/${blogId}/write`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(postData)
    })
    .then(response => {
        if (response.ok) {
            alert('게시글이 등록되었습니다.');
            location.href = `/blog/${blogId}`; // 작성 후 해당 블로그 메인으로 이동
        } else {
            return response.text().then(text => { throw new Error(text) });
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('게시글 등록 실패: ' + error.message);
    });
});