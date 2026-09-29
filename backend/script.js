// 서버 백엔드 Base URL (필요시 수정)
const BASE_URL = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', () => {
    fetchTodaySchedules();
});

// 1. 오늘 일정 조회 (GET /api/schedules/today)
async function fetchTodaySchedules() {
    try {
        const response = await fetch(`${BASE_URL}/api/schedules/today`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('일정을 불러오는 데 실패했습니다.');

        const data = await response.json();
        renderSchedules(data.schedules);
    } catch (error) {
        console.error('API Error:', error);
    }
}

// 2. 화면에 일정 리스트 렌더링
function renderSchedules(schedules) {
    const taskListContainer = document.getElementById('taskList');
    if (!taskListContainer) return;

    taskListContainer.innerHTML = ''; // 기존 목록 초기화

    let completedCount = 0;
    const totalCount = schedules.length;

    schedules.forEach(item => {
        if (item.completed) completedCount++;

        const taskLabel = document.createElement('label');
        taskLabel.className = 'task';

        taskLabel.innerHTML = `
            <input type="checkbox" 
                   class="task-check" 
                   data-schedule-id="${item.scheduleId}" 
                   ${item.completed ? 'checked' : ''} 
                   onchange="toggleScheduleComplete(${item.scheduleId}, this.checked)">
            <div class="task-info">
                <div class="task-name">${item.title}</div>
                <div class="task-time">${item.startTime} ~ ${item.endTime}</div>
            </div>
        `;

        taskListContainer.appendChild(taskLabel);
    });

    // 완료 개수 업데이트
    document.getElementById('completedCount').textContent = completedCount;
    document.getElementById('totalCount').textContent = totalCount;
}

// 3. 일정 완료 처리 (PATCH /api/schedules/{scheduleId}/complete)
async function toggleScheduleComplete(scheduleId, isChecked) {
    try {
        const response = await fetch(`${BASE_URL}/api/schedules/${scheduleId}/complete`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) throw new Error('완료 처리에 실패했습니다.');

        const data = await response.json();
        console.log('완료 처리 결과:', data);

        // 변경 후 최신 목록 다시 불러오기
        fetchTodaySchedules();
    } catch (error) {
        console.error('API Error:', error);
        alert('완료 상태 변경 중 오류가 발생했습니다.');
    }
}