import uuid from "uuid-v4";

export function genId() {
  return uuid();
}

export function deepCopy(obj) {
  return JSON.parse(JSON.stringify(obj));
}

export function getNowFormData(allData, id) {
  var formData = allData[id];
  if (formData === undefined || formData == null) {
    formData = {
      id: id,
      data: {}
    };
  }

  if (formData.id === undefined || formData.id === null) {
    formData.id = -1000;
  }

  if (formData.data === undefined || formData.data === null) {
    formData.data = {};
  }
  return deepCopy(formData);
}
