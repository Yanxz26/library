// ============================================
// useDict — 字典标签工具
// 统一所有页面的状态/类型标签映射
// ============================================

const DICTS = {
  // 用户类型
  userType: {
    1: { label: '学生', tag: 'success' },
    2: { label: '教师', tag: 'warning' },
    3: { label: '管理员', tag: 'danger' },
  },
  // 用户状态
  userStatus: {
    0: { label: '已禁用', tag: 'danger' },
    1: { label: '正常', tag: 'success' },
  },
  // 借阅状态
  borrowStatus: {
    1: { label: '借阅中', tag: 'primary' },
    2: { label: '已归还', tag: 'success' },
    3: { label: '已逾期', tag: 'danger' },
  },
  // 图书状态
  bookStatus: {
    0: { label: '已下架', tag: 'danger' },
    1: { label: '在架', tag: 'success' },
  },
  // 预约状态
  reserveStatus: {
    1: { label: '预约中', tag: 'warning' },
    2: { label: '已借阅', tag: 'success' },
    3: { label: '已过期', tag: 'info' },
  },
  // 付款状态
  payStatus: {
    0: { label: '待缴费', tag: 'danger' },
    1: { label: '已缴费', tag: 'success' },
    2: { label: '已减免', tag: 'info' },
  },
  // 损耗类型
  lossType: {
    1: { label: '丢失', tag: 'danger' },
    2: { label: '损坏', tag: 'warning' },
    3: { label: '其他', tag: 'info' },
  },
  // 日志类型
  logType: {
    1: { label: '操作日志', tag: 'primary' },
    2: { label: '业务日志', tag: 'success' },
    3: { label: '异常日志', tag: 'danger' },
  },
}

/**
 * 获取字典标签
 * @param {string} dict - 字典名 (如 'userType', 'borrowStatus')
 * @param {number|string} value - 字典值
 * @returns {string} 标签文本
 */
export function useDict() {
  function label(dict, value) {
    return DICTS[dict]?.[value]?.label ?? '未知'
  }

  function tag(dict, value) {
    return DICTS[dict]?.[value]?.tag ?? 'info'
  }

  function item(dict, value) {
    return DICTS[dict]?.[value] ?? { label: '未知', tag: 'info' }
  }

  return { label, tag, item, DICTS }
}
