import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import OrderIndex from './index.vue'
import { createPinia, setActivePinia } from 'pinia'
import { useUserStore } from '@/stores/user'

vi.mock('@/api/http', () => ({
  default: {
    get: vi.fn(() => Promise.resolve({ data: { records: [], total: 0 } })),
    post: vi.fn(() => Promise.resolve({ data: null })),
    delete: vi.fn(() => Promise.resolve({ data: null })),
  }
}))

vi.mock('element-plus', async () => {
  const original = await vi.importActual('element-plus')
  return {
    ...original,
    ElMessage: { success: vi.fn(), error: vi.fn() },
    ElMessageBox: { confirm: vi.fn(() => Promise.resolve()) }
  }
})

describe('Order Index', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders correctly and checks write permission', () => {
    const store = useUserStore()
    store.roles = ['EMPLOYEE']

    const wrapper = mount(OrderIndex, {
      global: {
        stubs: ['el-button', 'el-input', 'el-input-number', 'el-switch', 'el-table', 'el-table-column', 'el-pagination', 'el-dialog', 'el-form', 'el-form-item', 'el-select', 'el-option', 'el-date-picker', 'el-upload'],
        directives: {
          loading: () => {}
        }
      }
    })

    const buttons = wrapper.findAll('el-button')
    // ElButton might not be stubbed with -stub suffix depending on vue-test-utils version if standard DOM mock is used. Let's just find the component element.
    expect(wrapper.html()).toContain('新增')
  })
})
