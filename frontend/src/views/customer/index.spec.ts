import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import CustomerIndex from './index.vue'
import { createPinia, setActivePinia } from 'pinia'

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

describe('Customer Index', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('renders correctly', () => {
    const wrapper = mount(CustomerIndex, {
      global: {
        stubs: ['el-button', 'el-input', 'el-table', 'el-table-column', 'el-pagination', 'el-dialog', 'el-form', 'el-form-item', 'el-upload'],
        directives: {
          loading: () => {}
        }
      }
    })

    expect(wrapper.html()).toContain('客户管理')
  })
})
