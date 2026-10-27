// ========================================
// FocusGrow
// 목표 관리 및 화면 전환
// ========================================


// 1. 오늘 날짜 표시
const todayDate = document.getElementById("todayDate");

const today = new Date();

todayDate.textContent = today.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long"
});


// ========================================
// 2. 목표 데이터 불러오기
// ========================================

// 브라우저에 저장된 목표 데이터
const savedTasks = localStorage.getItem("focusGrowTasks");

// 저장된 데이터가 있으면 불러오고,
// 없으면 테스트용 목표 3개 사용
let tasks = savedTasks !== null
    ? JSON.parse(savedTasks)
    : [
        {
            id: 1,
            name: "자료구조 공부하기",
            time: "30분",
            category: "전공",
            completed: false
        },
        {
            id: 2,
            name: "영어 단어 암기",
            time: "20분",
            category: "영어",
            completed: false
        },
        {
            id: 3,
            name: "알고리즘 문제 풀기",
            time: "40분",
            category: "코딩",
            completed: false
        }
    ];


// ========================================
// 3. 경험치 설정
// ========================================

const EXP_PER_TASK = 20; //하나 완료시 경험치 20 획득

// 타이머 완료 시 지급하는 경험치
const EXP_PER_STUDY = 20;

// 타이머로 획득한 누적 경험치 불러오기
let studyExp = Number(
    localStorage.getItem("focusGrowStudyExp") || 0
);

// 타이머 경험치 저장 함수
function saveStudyExp() {
    localStorage.setItem("focusGrowStudyExp", String(studyExp));
}

const maxExp = 100; //레벨업에 필요한 경험치 100
//목표 6개 완료하면 6*20 =120 exp
//레벨이 올라가도 누적 경험치가 사라지는게 아니라 다음레벨을 계산하는데 사용함



// ========================================
// 4. 목표 저장 함수
// ========================================

//목표 데이터를 브라우저 저장 공간에 보관하라는 명령어
function saveTasks() {
    localStorage.setItem(
        "focusGrowTasks",
        JSON.stringify(tasks)
    );
}


// ========================================
// 5. 목표 목록 출력
// ========================================

function renderTasks() {

    const taskList = document.getElementById("taskList");

    // 기존 목록 지우기
    taskList.innerHTML = "";

    tasks.forEach((task) => {

        // 목표 한 줄 생성
        const taskElement = document.createElement("div");
        taskElement.className = "task";

        // 체크박스
        const checkbox = document.createElement("input");
        checkbox.type = "checkbox";
        checkbox.checked = task.completed;

        checkbox.addEventListener("change", () => {

            task.completed = checkbox.checked;

            // 변경된 체크 상태 저장
            saveTasks();

            // 완료 개수와 경험치 갱신
            updateCompletedCount();
            updateExperience();

        });

        // 목표 정보
        const taskInfo = document.createElement("div");
        taskInfo.className = "task-info";

        const taskName = document.createElement("div");
        taskName.className = "task-name";
        taskName.textContent = task.name;

        const taskTime = document.createElement("div");
        taskTime.className = "task-time";
        taskTime.textContent = task.time;

        const tag = document.createElement("span");
        tag.className = "tag";
        tag.textContent = task.category;

        taskInfo.appendChild(taskName);
        taskInfo.appendChild(taskTime);

        taskElement.appendChild(checkbox);
        // 수정 버튼 생성
const editButton = document.createElement("button");

editButton.type = "button";
editButton.className = "task-edit-btn";
editButton.textContent = "✏️";
editButton.title = "목표 수정";
editButton.setAttribute("aria-label", "목표 수정");

editButton.addEventListener("click", () => {
    editTask(task.id);
});


// 삭제 버튼 생성
const deleteButton = document.createElement("button");

deleteButton.type = "button";
deleteButton.className = "task-delete-btn";
deleteButton.textContent = "🗑️";
deleteButton.title = "목표 삭제";
deleteButton.setAttribute("aria-label", "목표 삭제");

deleteButton.addEventListener("click", () => {
    deleteTask(task.id);
});


// 수정·삭제 버튼을 묶는 영역
const taskActions = document.createElement("div");
taskActions.className = "task-actions";

taskActions.appendChild(editButton);
taskActions.appendChild(deleteButton);


// 목표 한 줄 완성
taskElement.appendChild(checkbox);
taskElement.appendChild(taskInfo);
taskElement.appendChild(tag);
taskElement.appendChild(taskActions);

taskList.appendChild(taskElement);

    });

    updateCompletedCount();
    updateExperience();
    
}


// ========================================
// 6. 완료 개수 업데이트
// ========================================
function updateCompletedCount() {

    const completedCount = tasks.filter(
        task => task.completed
    ).length;

    document.getElementById("completedCount").textContent =
        completedCount;

    document.getElementById("totalCount").textContent =
        tasks.length;
}


// ========================================
// 목표 수정 기능
// ========================================

function editTask(taskId) {

    // 수정할 목표 찾기
    const task = tasks.find(task => task.id === taskId);

    if (!task) {
        return;
    }

    // 새로운 목표 이름 입력
    const newName = prompt("목표 이름을 수정하세요.", task.name);

    // 취소 버튼을 누른 경우
    if (newName === null) {
        return;
    }

    // 빈 이름 방지
    if (newName.trim() === "") {
        alert("목표 이름은 비워 둘 수 없어!");
        return;
    }

    // 새로운 공부 시간 입력
    const newTime = prompt(
        "공부 시간을 입력해 줘. (분)",
        parseInt(task.time, 10)
    );

    if (newTime === null) {
        return;
    }

    const minutes = Number(newTime);

    if (!Number.isInteger(minutes) || minutes < 1 || minutes > 600) {
        alert("공부 시간은 1분부터 600분까지 입력하세요.");
        return;
    }

    // 새로운 분류 입력
    const newCategory = prompt(
        "분류를 입력하세요. (전공/영어/코딩/기타)",
        task.category
    );

    if (newCategory === null) {
        return;
    }

    const allowedCategories = ["전공", "영어", "코딩", "기타"];

    if (!allowedCategories.includes(newCategory.trim())) {
        alert("전공, 영어, 코딩, 기타 중 하나를 입력하세요.");
        return;
    }

    // 수정한 내용 반영
    task.name = newName.trim();
    task.time = minutes + "분";
    task.category = newCategory.trim();

    // 저장 및 화면 갱신
    saveTasks();
    renderTasks();

    alert("목표가 수정되었습니다.");
}


// ========================================
// 목표 삭제 기능
// ========================================

function deleteTask(taskId) {

    const task = tasks.find(task => task.id === taskId);

    if (!task) {
        return;
    }

    const confirmed = confirm(
        `"${task.name}" 목표를 삭제하시겠습니까?`
    );

    // 취소를 누르면 삭제하지 않음
    if (!confirmed) {
        return;
    }

    // 해당 목표를 제외한 목록으로 변경
    tasks = tasks.filter(task => task.id !== taskId);

    // 변경 내용 저장
    saveTasks();

    // 화면 갱신
    renderTasks();

    alert("목표가 삭제되었습니다.");
}


// ========================================
// 경험치 및 레벨업 시스템
// ========================================

// 이전에 확인한 레벨 저장
let previousLevel = Number(
    localStorage.getItem("focusGrowPreviousLevel") || 1
);

// ========================================
// 레벨업 축하 팝업 기능
// ========================================

function showLevelUpModal(level, unlockedItems) {

    // 팝업 요소 가져오기
    const modal = document.getElementById("levelUpModal");
    const levelNumber = document.getElementById("levelUpNumber");
    const levelName = document.getElementById("levelUpName");
    const levelItem = document.getElementById("levelUpItem");

    // 레벨업 아이템 이미지 요소
    const levelItemImage =
        document.getElementById("levelUpItemImage");

    // 레벨별 성장 이름
    const levelNames = {
        1: "새싹 🌱",
        2: "성장 중 🌿",
        3: "집중 달인 📚",
        4: "공부 고수 ⭐"
    };

    // 레벨 표시
    levelNumber.textContent = `⭐ Lv.${level} ⭐`;

    // 성장 이름 표시
    levelName.textContent =
        levelNames[level] || "공부 마스터 👑";

    // 새로 해금한 아이템 표시
    if (unlockedItems.length > 0) {
        levelItem.textContent = unlockedItems.join(", ");
    } else {
        levelItem.textContent = "⭐ 레벨 상승!";
    }

    // ========================================
    // 레벨별 해금 아이템 이미지
    // ========================================

    const unlockImages = {
        2: "assets/pixel/window.png",
        3: "assets/pixel/lamp1.png",
        4: "assets/pixel/cat1.png",
        5: "assets/pixel/plant1.png"
    };

    // 이번 레벨에 해당하는 이미지 확인
    const itemImagePath = unlockImages[level];

    if (levelItemImage) {

        if (itemImagePath && unlockedItems.length > 0) {

            // 아이템 이미지 설정
            levelItemImage.src = itemImagePath;

            // 이미지 설명
            levelItemImage.alt = unlockedItems.join(", ");

            // 숨겨진 이미지 표시
            levelItemImage.hidden = false;

        } else {

            // 해금 아이템이 없다면 이미지 숨기기
            levelItemImage.hidden = true;
        }
    }

    // 팝업 표시
    modal.hidden = false;
}

    // 확인 버튼을 누르면 팝업 닫기
    const levelUpCloseBtn =
        document.getElementById("levelUpCloseBtn");

    levelUpCloseBtn.addEventListener("click", () => {

        document.getElementById("levelUpModal").hidden = true;

    });

function updateExperience() {

    // 완료한 목표 개수
    const completedCount = tasks.filter(
        task => task.completed
    ).length;

    // 목표 완료로 획득한 경험치
    const taskExp = completedCount * EXP_PER_TASK;

    // 목표 경험치 + 타이머 경험치 합산
    const totalExp = taskExp + studyExp;


    // 현재 레벨 계산
    const currentLevel = Math.floor(totalExp / maxExp) + 1;

    // ========================================
    // 레벨업 감지 및 축하 알림
    // ========================================

    // 현재 레벨이 이전 레벨보다 높아졌는지 확인
    if (currentLevel > previousLevel) {

        // 레벨별 해금 아이템
        const unlockNames = {
            2: "🪟 창문",
            3: "💡 조명",
            4: "🐱 고양이",
            5: "🌿 화분"
        };

        // 이번에 새로 해금한 아이템 확인
        const unlockedItems = [];

        for (let level = previousLevel + 1; level <= currentLevel; level++) {
            if (unlockNames[level]) {
                unlockedItems.push(unlockNames[level]);
            }
        }

        // 축하 메시지 만들기
        let message = `🎉 레벨업!\nLv.${currentLevel} 달성`;

        if (unlockedItems.length > 0) {
            message += `\n\n아이템 획득\n${unlockedItems.join(", ")}`;
        }

        alert(message);
    }

    // 현재 레벨 저장
    previousLevel = currentLevel;

    localStorage.setItem(
        "focusGrowPreviousLevel",
        String(previousLevel)
    );

    // ========================================
    // 화면에 현재 레벨 표시
    // ========================================

    // 공부방 레벨 표시
    const roomLevel = document.getElementById("roomLevel");

    if (roomLevel) {
        roomLevel.textContent = `Lv.${currentLevel}`;
    }

    // 나의 성장 레벨 표시
    const userLevel = document.getElementById("userLevel");

    if (userLevel) {
        // 레벨별 성장 이름
        const levelNames = {
            1: "새싹 🌱",
            2: "성장 중 🌿",
            3: "집중 달인 📚",
            4: "공부 고수 ⭐"
        };

        // Lv.5 이상은 공부 마스터
        const levelName = levelNames[currentLevel] || "공부 마스터 👑";

        userLevel.textContent = `Lv.${currentLevel} ${levelName}`;
    }

    // 현재 레벨에서 쌓인 경험치
    const currentExp = totalExp % maxExp;

    // 경험치 진행률
    const expPercentage = (currentExp / maxExp) * 100;

    // 경험치 숫자 표시
    document.getElementById("currentExp").textContent =
        currentExp;

    document.getElementById("maxExp").textContent =
        maxExp;

    // 경험치 막대 업데이트
    document.getElementById("expProgressBar").style.width =
        expPercentage + "%";

    // 다음 레벨까지 필요한 경험치
    const remainingExp = maxExp - currentExp;

    document.getElementById("nextLevelText").textContent =
        `Lv.${currentLevel} · 다음 레벨까지 ${remainingExp} EXP`;

// ========================================
// 레벨에 따른 공부방 아이템 해금
// ========================================

// ========================================
// 레벨별 공부방 아이템 영구 해금
// ========================================

// 아이템 ID와 해금 레벨 설정
const roomUnlocks = [
    { id: "windowItem", level: 2 },
    { id: "lampItem", level: 3 },
    { id: "catItem", level: 4 },
    { id: "plantItem", level: 5 }
];

// 아이템마다 해금 여부 확인
roomUnlocks.forEach(item => {

    // HTML에서 해당 아이템 찾기
    const element = document.getElementById(item.id);

    // 아이템 획득 기록을 저장할 이름
    const storageKey = `focusGrowUnlocked-${item.id}`;

    // 이전에 획득한 적이 있는지 확인
    let unlocked =
        localStorage.getItem(storageKey) === "true";

    // 현재 레벨이 해금 레벨 이상이면 획득
    if (currentLevel >= item.level) {
        unlocked = true;

        // 획득 기록 저장
        localStorage.setItem(storageKey, "true");
    }

    // 획득한 아이템만 화면에 표시
    if (element) {
        element.hidden = !unlocked;
    }

});

}


// ========================================
// 8. 화면 전환 기능
// ========================================

// 홈 화면에 있는 영역들
const homeSections = document.querySelectorAll(
    ".room-section, .card:not(.goal-page):not(.timer-page):not(.record-page):not(.my-page)"
);

// 목표 설정 화면
const goalPage = document.getElementById("goalPage");

// 하단 메뉴
const menuItems = document.querySelectorAll(".menu-item");

// ========================================
// 화면 전환 기능
// ========================================

function showPage(pageName) {

    // 모든 화면 숨기기
    homeSections.forEach((section) => {
        section.hidden = true;
    });

    goalPage.hidden = true;

    timerPage.hidden = true;

    recordPage.hidden = true;

    // 메뉴 선택 표시 초기화
    menuItems.forEach((menu) => {
        menu.classList.remove("active");
    });

    // 현재 선택한 메뉴 표시
    const selectedMenu = document.querySelector(
        `.menu-item[data-menu="${pageName}"]`
    );

    if (selectedMenu) {
        selectedMenu.classList.add("active");
    }

    // 홈 화면
    if (pageName === "home") {

        homeSections.forEach((section) => {
            section.hidden = false;
        });

    }

    // 목표 설정 화면
    else if (pageName === "goal") {

        goalPage.hidden = false;

    }

    // 기록 조회 화면
    else if (pageName === "record") {

        recordPage.hidden = false;

        // 저장된 공부 기록을 불러와서 출력
        renderStudyRecords();

    }

    window.scrollTo(0, 0);
}


// ========================================
// 9. 메뉴 클릭 이벤트
// ========================================

menuItems.forEach((menu) => {

    menu.addEventListener("click", (event) => {

        event.preventDefault();

        const pageName = menu.dataset.menu;

        showPage(pageName);

    });

});


// ========================================
// 10. 목표 추가 기능
// ========================================

const goalForm = document.getElementById("goalForm");

goalForm.addEventListener("submit", (event) => {

    // 폼 제출 시 페이지 새로고침 방지
    event.preventDefault();

    // 사용자가 입력한 값 가져오기
    const goalName = document
        .getElementById("goalName")
        .value.trim();

    const goalTime = Number(
        document.getElementById("goalTime").value
    );

    const goalCategory = document
        .getElementById("goalCategory")
        .value;

    // 입력값 검사
    if (goalName === "") {
        alert("목표 이름을 입력해 줘!");
        return;
    }

    if (!Number.isInteger(goalTime) || goalTime < 1 || goalTime > 600) {
        alert("공부 시간은 1분부터 600분까지 입력해 줘!");
        return;
    }

    // 새로운 목표 만들기
    const newTask = {
        id: Date.now(),
        name: goalName,
        time: goalTime + "분",
        category: goalCategory,
        completed: false
    };

    // 목표 배열에 추가
    tasks.push(newTask);

    // 브라우저에 저장
    saveTasks();

    // 홈 화면 목표 목록 갱신
    renderTasks();

    // 입력창 초기화
    goalForm.reset();

    // 홈 화면으로 이동 
    // (목표 등록이 끝나면 홈화면으로 돌아가도록 하는 명령어)
    showPage("home");

    alert("새로운 목표가 추가되었습니다.");

});


// ========================================
// 11. 공부 시작 버튼
// ========================================

// ========================================
// 집중 공부 타이머
// ========================================

// 기본 집중 시간: 25분
//const FOCUS_TIME = 25 * 60;
// 테스트용 타이머 (10초)
const FOCUS_TIME = 10;

// 타이머 상태
let remainingSeconds = FOCUS_TIME;
let timerInterval = null;
let isPaused = false;

// 실제 집중한 시간(초)
let studiedSeconds = 0;

// 오늘 공부한 시간 불러오기
const studyDateKey = new Date().toLocaleDateString("en-CA");

const studyStorageKey = `focusGrowStudyTime-${studyDateKey}`;

let todayStudiedSeconds = Number(
    localStorage.getItem(studyStorageKey) || 0
);

// ========================================
// 캐릭터 상태 변경 기능
// ========================================


function changeCharacter(state) {

    const characterImage =
        document.getElementById("characterImage");

    const timerCharacterImage =
        document.getElementById("timerCharacterImage");

    const characterImages = {
        idle: "assets/pixel/character_idle.png",
        study: "assets/pixel/character_study.png",
        happy: "assets/pixel/character_happy.png",
        tired: "assets/pixel/character_tired.png"
    };

    const imagePath = characterImages[state];

    if (!imagePath) {
        return;
    }

    if (characterImage) {
        characterImage.src = imagePath;
    }

    if (timerCharacterImage) {
        timerCharacterImage.src = imagePath;
        timerCharacterImage.classList.remove("studying");

        if (state === "study") {
            timerCharacterImage.classList.add("studying");
        }
    }
}


// HTML 요소 가져오기
const startStudyBtn = document.getElementById("startStudyBtn");
const timerPage = document.getElementById("timerPage");
const timerDisplay = document.getElementById("timerDisplay");
const timerStatus = document.getElementById("timerStatus");
const timerProgressBar = document.getElementById("timerProgressBar");
const pauseTimerBtn = document.getElementById("pauseTimerBtn");
const endTimerBtn = document.getElementById("endTimerBtn");
const todayStudyTime = document.getElementById("todayStudyTime");


// ========================================
// 1. 시간을 00:00 형식으로 표시
// ========================================

function formatTime(seconds) {

    const minutes = Math.floor(seconds / 60);
    const secs = seconds % 60;

    return (
        String(minutes).padStart(2, "0") +
        ":" +
        String(secs).padStart(2, "0")
    );
}


// ========================================
// 2. 타이머 화면 업데이트
// ========================================

function updateTimerDisplay() {

    timerDisplay.textContent = formatTime(remainingSeconds);

    // 남은 시간 비율
    const percentage = (remainingSeconds / FOCUS_TIME) * 100;

    timerProgressBar.style.width = percentage + "%";

    // 오늘 누적 공부 시간 표시
    const totalSeconds = todayStudiedSeconds + studiedSeconds;

    const totalMinutes = Math.floor(totalSeconds / 60);

    if (todayStudyTime) {
    todayStudyTime.textContent = totalMinutes + "분";
}
}


// ========================================
// 3. 타이머 실행
// ========================================

function runTimer() {

    // 중복 실행 방지
    if (timerInterval !== null) {
        return;
    }

    timerInterval = setInterval(() => {

        if (remainingSeconds > 0) {

            remainingSeconds--;
            studiedSeconds++;

            updateTimerDisplay();

        }

        // 25분이 끝났을 때
        if (remainingSeconds <= 0) {

            finishStudy(true);

        }

    }, 1000);
}


// ========================================
// 4. 공부 시작
// ========================================

startStudyBtn.addEventListener("click", () => {

    // 타이머 초기화
    remainingSeconds = FOCUS_TIME;
    studiedSeconds = 0;
    isPaused = false;

    // 버튼과 안내 문구 초기화
    pauseTimerBtn.textContent = "⏸ 일시정지";
    timerStatus.textContent = "🌱 집중하고 있어요!";

    // 홈 화면 숨기기
    showPage("timer");

    // 타이머 화면 표시
    timerPage.hidden = false;

    // 타이머 숫자 업데이트
    updateTimerDisplay();

    // 타이머 시작
    runTimer();

    // 캐릭터 공부 상태로 변경
    changeCharacter("study");

});

// ========================================
// 5. 일시정지 / 다시 시작
// ========================================

pauseTimerBtn.addEventListener("click", () => {

    if (isPaused) {

        // 다시 시작
        isPaused = false;

        pauseTimerBtn.textContent = "⏸ 일시정지";
        timerStatus.textContent = "🌱 집중하고 있어요!";

        // 캐릭터 공부 상태
        changeCharacter("study");

        // 타이머 다시 실행
        runTimer();

    } else {

        // 일시정지
        isPaused = true;

        clearInterval(timerInterval);
        timerInterval = null;

        pauseTimerBtn.textContent = "▶ 다시 시작";
        timerStatus.textContent = "☕ 잠시 쉬고 있어요!";

        // 캐릭터 피곤한 상태
        changeCharacter("tired");

    }

});


// ========================================
// 6. 공부 종료
// ========================================

endTimerBtn.addEventListener("click", () => {

    const confirmed = confirm(
        "공부를 종료할까? 지금까지 공부한 시간은 저장돼."
    );

    if (!confirmed) {
        return;
    }

    finishStudy(false);

});


// ========================================
// 7. 공부 기록 저장 및 종료
// ========================================

function finishStudy(isCompleted) {

    // 타이머 정지
    clearInterval(timerInterval);
    timerInterval = null;

    // 이번에 공부한 시간을 오늘 기록에 추가
    todayStudiedSeconds += studiedSeconds;

    localStorage.setItem(
        studyStorageKey,
        String(todayStudiedSeconds)
    );

    // 이번 공부 시간 초기화
    studiedSeconds = 0;

    // 공부를 끝까지 완료한 경우
    if (isCompleted) {
        // 타이머 완료 보상 지급
        studyExp += EXP_PER_STUDY;

        // 획득한 경험치 저장
        saveStudyExp();

        // 경험치 및 레벨 갱신
        updateExperience();

        // 기쁜 표정으로 변경
        changeCharacter("happy");

    // 타이머 화면 캐릭터 가져오기
    const timerCharacterImage =
        document.getElementById("timerCharacterImage");

    // 점프 애니메이션 시작
    if (timerCharacterImage) {
        timerCharacterImage.classList.remove("studying");
        timerCharacterImage.classList.add("celebrating");
    }

    timerStatus.textContent = "🎉 공부 완료!";

    // 3초 동안 점프한 뒤 홈 화면으로 이동
    setTimeout(() => {

        if (timerCharacterImage) {
            timerCharacterImage.classList.remove("celebrating");
        }

        timerPage.hidden = true;
        showPage("home");

        alert("🎉 집중 공부 완료!");

    }, 3000);

    } else {

        // 중간 종료 시에는 바로 홈으로 이동
        changeCharacter("idle");

        timerPage.hidden = true;
        showPage("home");

        alert("🌱 공부 시간 저장완료");
    }

}


// ========================================
// 8. 처음 공부 시간 표시
// ========================================

updateTimerDisplay();



// ========================================
// 학습 기록 조회 기능
// ========================================

// 기록 조회 화면
const recordPage = document.getElementById("recordPage");

// 오늘 날짜를 YYYY-MM-DD 형식으로 변환
function getLocalDateKey(date) {

    const year = date.getFullYear();

    const month = String(
        date.getMonth() + 1
    ).padStart(2, "0");

    const day = String(
        date.getDate()
    ).padStart(2, "0");

    return `${year}-${month}-${day}`;
}


// 초 단위를 공부 시간 문자열로 변환
function formatStudyDuration(seconds) {

    const totalMinutes = Math.floor(seconds / 60);

    const hours = Math.floor(totalMinutes / 60);

    const minutes = totalMinutes % 60;

    if (hours > 0) {
        return `${hours}시간 ${minutes}분`;
    }

    return `${minutes}분`;
}


// 저장된 날짜별 공부 시간 불러오기
function getStudySeconds(dateKey) {

    const storageKey = `focusGrowStudyTime-${dateKey}`;

    return Number(
        localStorage.getItem(storageKey) || 0
    );
}


// ========================================
// 기록 조회 화면 출력
// ========================================

function renderStudyRecords() {

    const todayKey = getLocalDateKey(new Date());

    // 오늘 공부한 시간
    const todaySeconds = getStudySeconds(todayKey);

    document.getElementById("recordTodayTime").textContent =
        formatStudyDuration(todaySeconds);

    // 완료한 목표 개수
    const completedCount = tasks.filter(
        task => task.completed
    ).length;

    document.getElementById("recordCompletedTasks").textContent =
        completedCount + "개";


    // ========================================
    // 최근 7일 기록 준비
    // ========================================

    const weeklyRecords = [];

    for (let i = 6; i >= 0; i--) {

        const date = new Date();

        date.setHours(12, 0, 0, 0);
        date.setDate(date.getDate() - i);

        const dateKey = getLocalDateKey(date);

        const seconds = getStudySeconds(dateKey);

        weeklyRecords.push({
            date: date,
            seconds: seconds
        });

    }


    // ========================================
    // 최근 7일 막대그래프
    // ========================================

    const weeklyStudyChart =
        document.getElementById("weeklyStudyChart");

    weeklyStudyChart.innerHTML = "";

    // 7일 중 가장 긴 공부 시간을 기준으로 막대 높이 계산
    const maxSeconds = Math.max(
        ...weeklyRecords.map(record => record.seconds),
        1
    );

    weeklyRecords.forEach((record) => {

        const barItem = document.createElement("div");
        barItem.className = "weekly-bar-item";

        // 공부 시간
        const barTime = document.createElement("span");
        barTime.className = "weekly-bar-time";
        barTime.textContent = formatStudyDuration(record.seconds);

        // 막대 배경
        const barTrack = document.createElement("div");
        barTrack.className = "weekly-bar-track";

        // 실제 공부 시간 막대
        const barFill = document.createElement("div");
        barFill.className = "weekly-bar-fill";

        const heightPercentage =
            (record.seconds / maxSeconds) * 100;

        barFill.style.height = heightPercentage + "%";

        barTrack.appendChild(barFill);

        // 요일 표시
        const barDate = document.createElement("span");
        barDate.className = "weekly-bar-date";

        barDate.textContent = record.date.toLocaleDateString(
            "ko-KR",
            { weekday: "short" }
        );

        // 화면에 추가
        barItem.appendChild(barTime);
        barItem.appendChild(barTrack);
        barItem.appendChild(barDate);

        weeklyStudyChart.appendChild(barItem);

    });


    // ========================================
    // 날짜별 공부 시간 목록
    // ========================================

    const studyHistoryList =
        document.getElementById("studyHistoryList");

    studyHistoryList.innerHTML = "";

    weeklyRecords.slice().reverse().forEach((record) => {

        const historyItem = document.createElement("div");
        historyItem.className = "study-history-item";

        const historyDate = document.createElement("span");
        historyDate.className = "study-history-date";

        historyDate.textContent = record.date.toLocaleDateString(
            "ko-KR",
            {
                month: "long",
                day: "numeric",
                weekday: "short"
            }
        );

        const historyTime = document.createElement("strong");
        historyTime.className = "study-history-time";

        historyTime.textContent =
            formatStudyDuration(record.seconds);

        historyItem.appendChild(historyDate);
        historyItem.appendChild(historyTime);

        studyHistoryList.appendChild(historyItem);

    });

}

// ========================================
// 최초 화면 실행
// ========================================

renderTasks();
showPage("home");
/* ========================================
   FocusGrow 로그인 화면 표시
======================================== */

const loginPage = document.getElementById("loginPage");
const loginForm = document.getElementById("loginForm");
const signupMoveBtn = document.getElementById("signupMoveBtn");

/* 로그인 화면 표시 */
function showLoginPage() {
    // 기존 앱 화면 숨기기
    document.querySelectorAll(".app > :not(#loginPage)").forEach(element => {
        element.hidden = true;
    });

    // 로그인 화면 보이기
    loginPage.hidden = false;
}

/* ========================================
   FocusGrow 메인 화면 표시
======================================== */

function showMainPage() {

    // 시작, 로그인, 회원가입, 마이페이지 숨기기
    welcomePage.hidden = true;
    loginPage.hidden = true;
    signupPage.hidden = true;
    myPage.hidden = true;

    // 메인 화면의 상단 영역과 하단 메뉴 표시
    document.querySelector(".header").hidden = false;
    document.querySelector(".top-menu").hidden = false;

    // 기존 홈 화면 표시
    showPage("home");
}

/* ========================================
   임시 로그인 기능
======================================== */

loginForm.addEventListener("submit", function(event) {
    event.preventDefault();

    const loginId = document.getElementById("loginId").value.trim();
    const loginPassword = document.getElementById("loginPassword").value;

    if (!loginId || !loginPassword) {
        alert("아이디와 비밀번호를 입력해 주세요.");
        return;
    }

    // 실제 계정 인증은 추후 서버와 연결할 예정입니다.
    showMainPage();
});

/* 회원가입 버튼 클릭 */
signupMoveBtn.addEventListener("click", function() {
    alert("회원가입 화면은 다음 단계에서 추가할 예정입니다.");
});

/* ========================================
   FocusGrow 시작 화면 이동
======================================== */

const welcomePage = document.getElementById("welcomePage");
const welcomeLoginBtn = document.getElementById("welcomeLoginBtn");
const welcomeSignupBtn = document.getElementById("welcomeSignupBtn");

/* 시작 화면 표시 */
function showWelcomePage() {
    document.querySelectorAll(".app > :not(#welcomePage)").forEach(element => {
        element.hidden = true;
    });

    loginPage.hidden = true;
    welcomePage.hidden = false;
}

/* 시작 화면에서 로그인 화면으로 이동 */
welcomeLoginBtn.addEventListener("click", function() {
    welcomePage.hidden = true;
    showLoginPage();
});

/* 회원가입 화면 요소 */
const signupPage = document.getElementById("signupPage");
const signupForm = document.getElementById("signupForm");
const signupBackLoginBtn = document.getElementById("signupBackLoginBtn");

/* 회원가입 화면 표시 */
function showSignupPage() {
    document.querySelectorAll(".app > :not(#signupPage)").forEach(element => {
        element.hidden = true;
    });

    signupPage.hidden = false;
}

/* 시작 화면에서 회원가입 화면으로 이동 */
welcomeSignupBtn.addEventListener("click", function() {
    showSignupPage();
});

/* 회원가입 화면에서 로그인 화면으로 이동 */
signupBackLoginBtn.addEventListener("click", function() {
    showLoginPage();
});

/* 처음 접속하면 시작 화면 표시 */
showWelcomePage();


/* ========================================
   FocusGrow 회원가입 입력값 검사
======================================== */

signupForm.addEventListener("submit", function(event) {
    event.preventDefault();

    const nickname = document.getElementById("signupNickname").value.trim();
    const userId = document.getElementById("signupId").value.trim();
    const password = document.getElementById("signupPassword").value;
    const passwordConfirm = document.getElementById("signupPasswordConfirm").value;

    /* 빈칸 검사 */
    if (!nickname || !userId || !password || !passwordConfirm) {
        alert("모든 항목을 입력해 주세요.");
        return;
    }

    /* 비밀번호 길이 검사 */
    if (password.length < 8) {
        alert("비밀번호는 8자 이상 입력해 주세요.");
        return;
    }

    /* 비밀번호 일치 검사 */
    if (password !== passwordConfirm) {
        alert("비밀번호가 일치하지 않습니다. 다시 확인해 주세요.");
        return;
    }

    /* 입력값 검사 완료 */
    alert("입력하신 정보를 확인했습니다. 실제 회원가입 기능은 추후 연결됩니다.");
});

/* ========================================
   FocusGrow 마이페이지
======================================== */

const myPage = document.getElementById("myPage");
const profileBtn = document.getElementById("profileBtn");
const editProfileBtn = document.getElementById("editProfileBtn");
const logoutBtn = document.getElementById("logoutBtn");

/* 마이페이지 정보 표시 */
function updateMyPage() {

    // 기존 메인 화면에 표시되는 레벨과 경험치 사용
    const levelText = document.getElementById("userLevel").textContent;
    const currentExp = document.getElementById("currentExp").textContent;
    const maxExp = document.getElementById("maxExp").textContent;

    document.getElementById("mypageLevel").textContent = levelText;

    document.getElementById("mypageExpText").textContent =
        `${currentExp} / ${maxExp} EXP`;

    const progress = Number(maxExp) > 0
        ? (Number(currentExp) / Number(maxExp)) * 100
        : 0;

    document.getElementById("mypageExpProgress").style.width =
        `${Math.min(100, Math.max(0, progress))}%`;

    // 닉네임은 추후 실제 계정 정보와 연결
    document.getElementById("mypageNickname").textContent =
        "FocusGrow 사용자";

    // 공부 시간과 완료 목표는 기존 기록 데이터 연결 전까지 임시 표시
    document.getElementById("mypageTotalTime").textContent = "0분";
    document.getElementById("mypageCompletedGoals").textContent = "0개";
}

/* MY 버튼 클릭 */
profileBtn.addEventListener("click", function() {

    // 기존 화면 숨기기
    document.querySelectorAll(".app > :not(#myPage)").forEach(element => {
        element.hidden = true;
    });

    // 마이페이지 정보 갱신
    updateMyPage();

    // 마이페이지 표시
    myPage.hidden = false;
});

/* 프로필 수정 버튼 */
editProfileBtn.addEventListener("click", function() {
    alert("프로필 수정 기능은 다음 단계에서 연결할 예정입니다.");
});

/* 로그아웃 버튼 */
logoutBtn.addEventListener("click", function() {

    myPage.hidden = true;

    // 시작 화면 표시
    showWelcomePage();
});

/* 마이페이지에서 홈으로 돌아가기 */
document.getElementById("mypageBackBtn").addEventListener("click", function() {
    showMainPage();
});
