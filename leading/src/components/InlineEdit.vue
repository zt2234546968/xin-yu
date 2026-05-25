<template>
  <div class="inline-edit-wrapper">
    <div v-if="!editing" class="display-mode" @click="startEdit">
      <span class="display-value">{{ displayValue }}</span>
      <i class="el-icon-edit edit-icon"></i>
    </div>
    <div v-else class="edit-mode">
      <el-input
        v-if="type === 'text'"
        v-model="editValue"
        :size="size"
        :placeholder="placeholder"
        ref="input"
        @keyup.enter="saveEdit"
        @keyup.escape="cancelEdit"
        @blur="saveEdit"
      ></el-input>
      <el-input-number
        v-else-if="type === 'number'"
        v-model="editValue"
        :size="size"
        :placeholder="placeholder"
        ref="input"
        @keyup.enter="saveEdit"
        @keyup.escape="cancelEdit"
        @blur="saveEdit"
      ></el-input-number>
    </div>
  </div>
</template>

<script>
export default {
  name: 'InlineEdit',
  props: {
    value: {
      type: [String, Number],
      default: ''
    },
    displayValue: {
      type: [String, Number],
      default: '--'
    },
    type: {
      type: String,
      default: 'text',
      validator: val => ['text', 'number'].includes(val)
    },
    placeholder: {
      type: String,
      default: '点击编辑'
    },
    size: {
      type: String,
      default: 'small'
    }
  },
  data() {
    return {
      editing: false,
      editValue: null
    }
  },
  methods: {
    startEdit() {
      this.editValue = this.value
      this.editing = true
      this.$nextTick(() => {
        if (this.$refs.input && this.$refs.input.focus) {
          this.$refs.input.focus()
        }
      })
    },
    saveEdit() {
      if (this.editValue !== this.value) {
        this.$emit('input', this.editValue)
        this.$emit('save', this.editValue)
      }
      this.editing = false
    },
    cancelEdit() {
      this.editValue = this.value
      this.editing = false
    }
  }
}
</script>

<style scoped>
.inline-edit-wrapper {
  display: inline-block;
  width: 100%;
}

.display-mode {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.display-mode:hover {
  background-color: #f5f7fa;
}

.display-value {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.edit-icon {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.2s;
}

.display-mode:hover .edit-icon {
  opacity: 1;
}

.edit-mode {
  width: 100%;
}
</style>

