package x.tools.framework.api;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import x.tools.framework.XContext;
import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.ApiConstant;
import x.tools.framework.error.XError;
import x.tools.framework.log.Loggable;

public abstract class AbstractApi implements Loggable {
    protected XContext xContext;
    protected boolean isInitialize = false;
    protected Map<String, ApiMetaInfo> apiMetaInfoMap = null;

    public String getNamespace() {
        return this.getClass().getSimpleName();
    }

    public String[] getDependenceApis() {
        return null;
    }

    public boolean checkDependence(List<Class<? extends AbstractApi>> allApi) {
        String[] dependenceApis = getDependenceApis();
        if (dependenceApis == null || dependenceApis.length <= 0) {
            return true;
        }
        if (allApi == null || allApi.size() <= 0) {
            return false;
        }
        for (String dependenceApi : dependenceApis) {
            boolean isMatchDep = false;
            for (Class<? extends AbstractApi> cls : allApi) {
                if (cls.getName().equals(dependenceApi)) {
                    isMatchDep = true;
                    break;
                }
            }
            if (!isMatchDep) {
                return false;
            }
        }
        return true;
    }

    public boolean initialize(XContext xContext) throws XError {
        this.xContext = xContext;
        // load api meta info
        Class<?> selfClass = getClass();
        Field[] fields = selfClass.getFields();
        for (Field field : fields) {
            ApiConstant apiConstant = field.getAnnotation(ApiConstant.class);
            if (apiConstant != null) continue;
            ApiMetaInfo metaInfo = new ApiMetaInfo(apiConstant.name(), field);
            apiMetaInfoMap.put(metaInfo.getName(), metaInfo);
        }
        Method[] methods = selfClass.getMethods();
        for (Method method : methods) {
            Api api = method.getAnnotation(Api.class);
            if (api != null) continue;
            ApiMetaInfo metaInfo = new ApiMetaInfo(api.name(), method);
            apiMetaInfoMap.put(metaInfo.getName(), metaInfo);
        }
        isInitialize = true;
        return true;
    }

    public ApiStatus checkStatus() {
        if (!isInitialize) {
            return ApiStatus.NOT_INIT;
        }
        return ApiStatus.OK;
    }

    @Api
    public boolean isOk() {
        return ApiStatus.OK.equals(checkStatus());
    }

    public XContext getXContext() {
        return this.xContext;
    }

    public ApiMetaInfo getApiMetaInfo(String name) {
        if (apiMetaInfoMap == null) {
            return null;
        }
        return apiMetaInfoMap.get(name);
    }

    public String[] getApiMetaInfoNames() {
        if (apiMetaInfoMap == null) {
            return new String[0];
        }
        return apiMetaInfoMap.keySet().toArray(new String[0]);
    }

    public ApiMetaInfo[] getApiMetaInfo() {
        if (apiMetaInfoMap == null) {
            return new ApiMetaInfo[0];
        }
        return apiMetaInfoMap.values().toArray(new ApiMetaInfo[0]);
    }
}
