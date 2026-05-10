# 短期活动与长期活动功能说明

## 📋 核心概念

### 短期活动（Short-term Activity）
- **特点**：只开一天，一天内只有固定几个小时
- **示例**：今天下午2点-4点的反诈讲座
- **报名规则**：总人数限制，报满即止
- **结束后**：活动结束，不会再开始

### 长期活动（Long-term Activity）
- **特点**：持续多天，每天有固定的时间段开放
- **示例**：连续5天，每天上午9点-11点的反诈培训
- **报名规则**：按天报名，每天独立人数限制
- **灵活性**：用户可选择其中一天或多天报名
- **独立性**：某天满员不影响其他天的报名

---

## 🔧 数据库设计

### 1. activity表新增字段

```sql
-- 活动持续时间类型
activity_duration_type VARCHAR(20) DEFAULT 'short'
  - short: 短期活动
  - long: 长期活动

-- 持续天数（仅长期活动使用）
duration_days INT DEFAULT 1
```

### 2. activity_schedule表（长期活动的日程）

```sql
CREATE TABLE activity_schedule (
  id INT PRIMARY KEY AUTO_INCREMENT,
  activity_id INT NOT NULL,          -- 活动ID
  schedule_date DATE NOT NULL,       -- 日期
  start_time TIME NOT NULL,          -- 开始时间
  end_time TIME NOT NULL,            -- 结束时间
  max_participants INT DEFAULT 0,    -- 该天限制人数
  current_participants INT DEFAULT 0, -- 该天当前报名人数
  create_time DATETIME
);
```

**关键点**：
- 每个长期活动会生成多条日程记录（每天一条）
- 每天的`max_participants`相同（由管理员设置）
- 每天的`current_participants`独立统计

### 3. activity_signup表新增字段

```sql
-- 报名的日期（仅长期活动使用）
schedule_date DATE DEFAULT NULL
```

**作用**：
- 短期活动：该字段为NULL
- 长期活动：记录用户报名的具体日期

---

## 💻 后端逻辑

### 1. 创建活动时的日程生成

```java
// ActivityServiceImpl.add()
if ("long".equals(activity.getActivityDurationType())) {
    generateSchedules(activity); // 自动生成日程
}

private void generateSchedules(Activity activity) {
    LocalDate startDate = activity.getStartTime().toLocalDate();
    LocalTime startTime = activity.getStartTime().toLocalTime();
    LocalTime endTime = activity.getEndTime().toLocalTime();
    Integer durationDays = activity.getDurationDays();
    
    for (int i = 0; i < durationDays; i++) {
        ActivitySchedule schedule = new ActivitySchedule();
        schedule.setActivityId(activity.getId());
        schedule.setScheduleDate(startDate.plusDays(i)); // 第i天
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setMaxParticipants(activity.getMaxParticipants());
        schedule.setCurrentParticipants(0);
        activityScheduleMapper.insert(schedule);
    }
}
```

**示例**：
```
活动信息：
- 类型：长期活动
- 开始时间：2026-05-10 09:00
- 结束时间：2026-05-10 11:00
- 持续天数：3天
- 限制人数：50人/天

生成的日程：
1. 2026-05-10 09:00-11:00，限50人
2. 2026-05-11 09:00-11:00，限50人
3. 2026-05-12 09:00-11:00，限50人
```

### 2. 报名逻辑

#### 短期活动报名
```java
// 检查总人数限制
if (activity.getMaxParticipants() > 0 && 
    activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
    throw new CustomException("400", "报名人数已满");
}

// 更新总报名人数
activity.setCurrentParticipants(current + 1);
```

#### 长期活动报名
```java
// 1. 检查是否已报名该天
if (已报名同一天的记录) {
    throw new CustomException("400", "您已报名这一天的活动");
}

// 2. 检查指定日期的名额
ActivitySchedule schedule = getSchedule(activityId, scheduleDate);
if (schedule.getMaxParticipants() > 0 && 
    schedule.getCurrentParticipants() >= schedule.getMaxParticipants()) {
    throw new CustomException("400", "这一天报名人数已满");
}

// 3. 更新该天的报名人数
schedule.setCurrentParticipants(current + 1);
```

**关键区别**：
- 短期活动：检查`activity.currentParticipants`
- 长期活动：检查`activity_schedule.currentParticipants`（按天）

### 3. 取消报名逻辑

```java
private void updateParticipantsCount(ActivitySignup signup, Activity activity, boolean isAdd) {
    if ("long".equals(activity.getActivityDurationType())) {
        // 长期活动：更新对应日期的报名人数
        ActivitySchedule schedule = getSchedule(signup.getActivityId(), signup.getScheduleDate());
        schedule.setCurrentParticipants(schedule.getCurrentParticipants() + (isAdd ? 1 : -1));
    } else {
        // 短期活动：更新总报名人数
        activity.setCurrentParticipants(activity.getCurrentParticipants() + (isAdd ? 1 : -1));
    }
}
```

---

## 🎨 前端实现要点

### 1. 管理员端 - 创建活动

#### 活动类型选择
```vue
<el-form-item label="活动类型">
  <el-radio-group v-model="form.activityDurationType">
    <el-radio value="short">短期活动（一天）</el-radio>
    <el-radio value="long">长期活动（多天）</el-radio>
  </el-radio-group>
</el-form-item>
```

#### 持续天数（仅长期活动显示）
```vue
<el-form-item v-if="form.activityDurationType === 'long'" label="持续天数">
  <el-input-number v-model="form.durationDays" :min="2" :max="30" />
  <div style="color: #999; font-size: 12px;">
    活动将持续 {{ form.durationDays }} 天，每天相同时间段
  </div>
</el-form-item>
```

#### 限制人数说明
```vue
<el-form-item label="限制人数">
  <el-input-number v-model="form.maxParticipants" :min="0" :max="99999" />
  <div v-if="form.activityDurationType === 'long'" style="color: #999; font-size: 12px;">
    每天的限制人数（共 {{ form.durationDays }} 天，每天可报 {{ form.maxParticipants }} 人）
  </div>
  <div v-else style="color: #999; font-size: 12px;">
    总限制人数（0表示不限制）
  </div>
</el-form-item>
```

### 2. 用户端 - 活动详情

#### 短期活动显示
```vue
<div v-if="activity.activityDurationType === 'short'">
  <div>⏱️ 活动时间：{{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</div>
  <div>👥 报名情况：{{ activity.currentParticipants }} / {{ activity.maxParticipants || '不限' }}</div>
</div>
```

#### 长期活动显示
```vue
<div v-else>
  <div>📅 活动周期：{{ activity.durationDays }} 天</div>
  <div>⏰ 每天时间：{{ formatTime(activity.startTime) }} - {{ formatTime(activity.endTime) }}</div>
  
  <!-- 日程列表 -->
  <div v-for="schedule in schedules" :key="schedule.id">
    <div>{{ schedule.scheduleDate }}</div>
    <div>已报名：{{ schedule.currentParticipants }} / {{ schedule.maxParticipants }}</div>
    <el-button 
      @click="signupForDate(schedule.scheduleDate)"
      :disabled="isFull(schedule) || hasSignedUp(schedule.date)"
    >
      {{ getButtonText(schedule) }}
    </el-button>
  </div>
</div>
```

### 3. 报名对话框

#### 短期活动
```vue
<el-dialog title="活动报名">
  <!-- 直接填写报名信息，无需选择日期 -->
  <el-form-item label="真实姓名">...</el-form-item>
  <el-form-item label="手机号">...</el-form-item>
</el-dialog>
```

#### 长期活动
```vue
<el-dialog title="选择报名日期">
  <!-- 先选择日期 -->
  <el-radio-group v-model="selectedDate">
    <el-radio v-for="schedule in availableSchedules" :value="schedule.date">
      {{ schedule.date }} (剩余 {{ schedule.remaining }} 个名额)
    </el-radio>
  </el-radio-group>
  
  <!-- 然后填写报名信息 -->
  <el-form-item label="真实姓名">...</el-form-item>
  <el-form-item label="手机号">...</el-form-item>
</el-dialog>
```

---

## 📊 数据流程示例

### 场景1：创建长期活动

**管理员操作**：
1. 选择"长期活动"
2. 设置持续天数：3天
3. 设置时间：2026-05-10 09:00 至 2026-05-10 11:00
4. 设置限制人数：50人/天

**系统自动生成**：
```
activity表：
- id: 1
- activity_duration_type: "long"
- duration_days: 3
- max_participants: 50
- start_time: 2026-05-10 09:00:00
- end_time: 2026-05-10 11:00:00

activity_schedule表：
- id: 1, activity_id: 1, date: 2026-05-10, max: 50, current: 0
- id: 2, activity_id: 1, date: 2026-05-11, max: 50, current: 0
- id: 3, activity_id: 1, date: 2026-05-12, max: 50, current: 0
```

### 场景2：用户报名

**用户A报名5月10日**：
```
activity_signup表：
- id: 1, user_id: 1, activity_id: 1, schedule_date: 2026-05-10

activity_schedule表更新：
- id: 1, current_participants: 1 (从0变为1)
```

**用户B报名5月10日**：
```
activity_signup表：
- id: 2, user_id: 2, activity_id: 1, schedule_date: 2026-05-10

activity_schedule表更新：
- id: 1, current_participants: 2 (从1变为2)
```

**用户C报名5月11日**：
```
activity_signup表：
- id: 3, user_id: 3, activity_id: 1, schedule_date: 2026-05-11

activity_schedule表更新：
- id: 2, current_participants: 1 (从0变为1)
```

**结果**：
- 5月10日：2人报名（剩余48个名额）
- 5月11日：1人报名（剩余49个名额）
- 5月12日：0人报名（剩余50个名额）

**关键**：每天的报名互不影响！

### 场景3：某天满员

假设5月10日已有50人报名：

**用户D尝试报名5月10日**：
```
❌ 错误提示："2026-05-10 这一天的报名人数已满，无法继续报名"
```

**用户D可以报名5月11日或5月12日**：
```
✅ 成功报名其他日期
```

---

## ⚠️ 注意事项

### 1. 重复报名检查

**短期活动**：
```sql
SELECT * FROM activity_signup 
WHERE activity_id = ? AND user_id = ?
```

**长期活动**：
```sql
SELECT * FROM activity_signup 
WHERE activity_id = ? AND user_id = ? AND schedule_date = ?
```

**规则**：
- 短期活动：一个用户只能报名一次
- 长期活动：一个用户可以报名多天，但同一天只能报名一次

### 2. 人数限制独立性

**长期活动**：
- 每天的`max_participants`相同
- 每天的`current_participants`独立
- 某天满员不影响其他天

**短期活动**：
- 只有一个`max_participants`
- 只有一个`current_participants`
- 满员后所有人都不能报名

### 3. 取消报名的影响

**短期活动**：
```
用户取消 → activity.currentParticipants - 1
```

**长期活动**：
```
用户取消5月10日的报名 → 
  activity_schedule中5月10日的current_participants - 1
  （不影响5月11日、5月12日）
```

### 4. 退报审批

退报审批逻辑同样需要区分活动类型：
- 短期活动：减少`activity.currentParticipants`
- 长期活动：减少对应日期的`activity_schedule.current_participants`

---

## 🚀 API接口说明

### 1. 创建活动

```
POST /activity/add
Body: {
  title: "反诈培训",
  activityDurationType: "long",  // short或long
  durationDays: 3,                // 仅long需要
  startTime: "2026-05-10 09:00:00",
  endTime: "2026-05-10 11:00:00",
  maxParticipants: 50,
  ...
}
```

### 2. 报名活动

**短期活动**：
```
POST /activitySignUp/add
Body: {
  userId: 1,
  activityId: 1,
  realName: "张三",
  phone: "13800138000",
  ...
  // 不需要scheduleDate
}
```

**长期活动**：
```
POST /activitySignUp/add
Body: {
  userId: 1,
  activityId: 1,
  scheduleDate: "2026-05-10",  // 必须指定日期
  realName: "张三",
  phone: "13800138000",
  ...
}
```

### 3. 查询活动日程（长期活动）

```
GET /activitySchedule/selectByActivityId?activityId=1
Response: [
  {
    id: 1,
    scheduleDate: "2026-05-10",
    startTime: "09:00:00",
    endTime: "11:00:00",
    maxParticipants: 50,
    currentParticipants: 2
  },
  ...
]
```

---

## ✅ 测试清单

### 短期活动测试
- [ ] 创建活动时默认为短期活动
- [ ] 报名后总人数+1
- [ ] 满员后无法报名
- [ ] 取消报名后总人数-1
- [ ] 一个用户只能报名一次

### 长期活动测试
- [ ] 创建活动时自动生成日程
- [ ] 日程数量等于持续天数
- [ ] 每天的人数独立统计
- [ ] 某天满员不影响其他天
- [ ] 用户可以报名多天
- [ ] 同一天不能重复报名
- [ ] 取消某天的报名只影响该天人数
- [ ] 退报审批正确更新对应日期人数

### 边界情况测试
- [ ] 持续天数为1的长期活动（等同于短期）
- [ ] 限制人数为0（不限制）
- [ ] 跨月活动（如5月30日-6月2日）
- [ ] 删除活动时同时删除所有日程
- [ ] 修改活动时间时重新生成日程（待实现）

---

## 📝 相关文件清单

### 数据库
- `add_activity_duration_type.sql` - SQL脚本

### 后端
- `Activity.java` - 添加activityDurationType和durationDays字段
- `ActivitySchedule.java` - 添加maxParticipants和currentParticipants字段
- `ActivitySignup.java` - 添加scheduleDate字段
- `ActivityScheduleMapper.java` - 新建Mapper
- `ActivityServiceImpl.java` - 添加日程生成逻辑
- `ActivitySignUpServiceImpl.java` - 修改报名和取消逻辑

### 前端（待实现）
- `admin/Activity.vue` - 添加活动类型选择和日程设置
- `user/ActivityDetail.vue` - 显示活动类型和日程，支持按天报名

---

## 🎯 后续优化建议

1. **日程修改功能**：允许管理员修改某一天的时间或人数
2. **批量报名**：长期活动支持一次性报名多天
3. **日程冲突检测**：防止同一时间段创建多个活动
4. **日历视图**：用日历展示长期活动的日程安排
5. **报名统计**：按天统计报名人数和趋势
6. **自动清理**：活动结束后自动归档日程数据
