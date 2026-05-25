import axios, { type AxiosRequestConfig } from "axios";
import { ElMessage } from "element-plus";

export interface ApiResult<T = any> {
  code: number;
  message: string;
  data: T;
}

type AnyRecord = Record<string, any>;

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
  timeout: 10000
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      ElMessage.error("登录已过期，请重新登录");
      window.location.href = "/login";
      return Promise.reject(error);
    }

    const message = error.response?.data?.message || "请求失败，请稍后再试";
    ElMessage.error(message);
    return Promise.reject(error);
  }
);

const get = <T = any>(url: string, config?: AxiosRequestConfig) => api.get<unknown, ApiResult<T>>(url, config);
const post = <T = any>(url: string, data?: unknown, config?: AxiosRequestConfig) => api.post<unknown, ApiResult<T>>(url, data, config);
const put = <T = any>(url: string, data?: unknown, config?: AxiosRequestConfig) => api.put<unknown, ApiResult<T>>(url, data, config);
const del = <T = any>(url: string, config?: AxiosRequestConfig) => api.delete<unknown, ApiResult<T>>(url, config);

export const user = {
  login: (data: AnyRecord) => post("/user/login", data),
  register: (data: AnyRecord) => post("/user/register", data),
  list: () => get("/user/list"),
  update: (data: AnyRecord) => put("/user/update", data),
  info: (id: string) => get("/user/info", { params: { id } }),
  updatePassword: (id: string, oldPassword: string, newPassword: string) =>
    put("/user/password", null, { params: { id, oldPassword, newPassword } })
};

export const invitationCode = {
  generate: () => post("/invitationCode/generate"),
  validate: (code: string) => get("/invitationCode/validate", { params: { code } }),
  getByCode: (code: string) => get("/invitationCode/getByCode", { params: { code } }),
  list: () => get("/invitationCode/list"),
  updateRemark: (id: string, remark: string) => post("/invitationCode/updateRemark", null, { params: { id, remark } })
};

export const zhiping = {
  list: () => get("/zhiping/list"),
  getById: (id: string) => get("/zhiping/getById", { params: { id } }),
  create: (data: AnyRecord) => post("/zhiping/create", data),
  update: (id: string, data: AnyRecord) => put("/zhiping/update", data, { params: { id } }),
  delete: (id: string) => del("/zhiping/delete", { params: { id } }),
  updateStatus: (id: string, status: string) => post("/zhiping/updateStatus", null, { params: { id, status } }),
  updateFeedback: (id: string, feedbackLink?: string, feedbackImage?: string, channelId?: string) =>
    post("/zhiping/updateFeedback", null, { params: { id, feedbackLink, feedbackImage, channelId } }),
  generateCode: () => get<string>("/zhiping/generateCode")
};

export const country = {
  list: () => get("/country/list"),
  getById: (id: string) => get("/country/getById", { params: { id } }),
  getByName: (countryName: string) => get("/country/getByName", { params: { countryName } })
};

export const ceping = {
  list: () => get("/ceping/list"),
  getById: (id: string) => get("/ceping/getById", { params: { id } }),
  create: (data: AnyRecord) => post("/ceping/create", data),
  update: (id: string, data: AnyRecord) => put("/ceping/update", data, { params: { id } }),
  delete: (id: string) => del("/ceping/delete", { params: { id } }),
  generateCode: () => get<string>("/ceping/generateCode")
};

export const task = {
  list: (taskType: string) => get("/task/list", { params: { taskType } }),
  getById: (id: string) => get("/task/getById", { params: { id } }),
  create: (data: AnyRecord) => post("/task/create", data),
  update: (id: string, data: AnyRecord) => put("/task/update", data, { params: { id } }),
  delete: (id: string) => del("/task/delete", { params: { id } }),
  updateStatus: (id: string, status: string) => post("/task/updateStatus", null, { params: { id, status } }),
  updateFeedback: (id: string, feedbackLink?: string, feedbackImage?: string, channel?: string) =>
    post("/task/updateFeedback", null, { params: { id, feedbackLink, feedbackImage, channel } }),
  generateCode: (taskType: string) => get<string>("/task/generateCode", { params: { taskType } })
};

export const orderList = {
  list: () => get("/orderList/list"),
  create: (data: AnyRecord) => post("/orderList", data),
  getById: (id: string) => get(`/orderList/${id}`),
  getByCepingId: (cepingId: string) => get(`/orderList/ceping/${cepingId}`),
  update: (id: string, data: AnyRecord) => put(`/orderList/${id}`, data),
  delete: (id: string) => del(`/orderList/${id}`)
};

export default {
  user,
  invitationCode,
  zhiping,
  country,
  ceping,
  task,
  orderList
};
