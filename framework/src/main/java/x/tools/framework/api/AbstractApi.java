package x.tools.framework.api;

import android.content.ContextWrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import x.tools.framework.R;
import x.tools.framework.XContext;
import x.tools.framework.annotation.Api;
import x.tools.framework.annotation.ApiConstant;
import x.tools.framework.error.XError;
import x.tools.framework.log.Loggable;

public abstract class AbstractApi extends ContextWrapper implements Loggable {
    protected XContext xContext;
    protected boolean isInitialize = false;
    protected List<ApiMetaInfo> apiMetaInfoList = null;

    public AbstractApi() {
        super(null);
    }

    public String getNamespace() {
        return this.getClass().getSimpleName();
    }

    public String[] getDependenceApis() {
        return null;
    }

    public final String[] checkDependence(List<AbstractApi> allApi) {
        String[] dependenceApis = getDependenceApis();
        if (dependenceApis == null || dependenceApis.length <= 0) {
            return null;
        }
        if (allApi == null || allApi.size() <= 0) {
            return dependenceApis;
        }
        List<String> listApis = new ArrayList<>();
        for (String dependenceApi : dependenceApis) {
            boolean isMatchDep = false;
            for (AbstractApi api : allApi) {
                if (api.getClass().getName().equals(dependenceApi)) {
                    isMatchDep = true;
                    break;
                }
            }
            if (!isMatchDep) {
                listApis.add(dependenceApi);
            }
        }
        if (listApis.size() <= 0) return null;
        return listApis.toArray(new String[listApis.size()]);
    }

    public boolean initialize(XContext xContext) throws XError {
        attachBaseContext(xContext);

        this.xContext = xContext;
        List<ApiMetaInfo> apiMetaInfoList = new ArrayList<>();
        // load api meta info
        Class<?> selfClass = getClass();
        Field[] fields = selfClass.getFields();
        for (Field field : fields) {
            ApiConstant apiConstant = field.getAnnotation(ApiConstant.class);
            if (apiConstant == null) continue;
            ApiMetaInfo metaInfo = new ApiMetaInfo(this.getBaseContext(), apiConstant.name(), field);
            apiMetaInfoList.add(metaInfo);
        }
        Method[] methods = selfClass.getMethods();
        for (Method method : methods) {
            Api api = method.getAnnotation(Api.class);
            if (api == null) continue;
            ApiMetaInfo metaInfo = new ApiMetaInfo(this.getBaseContext(), api.name(), method);
            apiMetaInfoList.add(metaInfo);
        }
        this.apiMetaInfoList = Collections.unmodifiableList(apiMetaInfoList);
        isInitialize = true;
        return true;
    }

    public ApiStatus checkStatus() {
        if (!isInitialize) {
            return ApiStatus.NOT_INIT;
        }
        return ApiStatus.OK;
    }

    @Api(name = "checkStatus")
    public int _checkStatus() {
        return checkStatus().ordinal();
    }

    @Api
    public String statusDescription() {
        switch (checkStatus()) {
            case OK:
                return getString(R.string.X_TOOLS_OK);
            case NOT_INIT:
                return getString(R.string.X_TOOLS_API_NOT_INIT);
            case INIT_FAIL:
                return getString(R.string.X_TOOLS_API_INIT_FAIL);
            case NEED_PERMISSION:
                return getString(R.string.X_TOOLS_API_NEED_PERMISSION);
            case NOT_RUNNING:
                return getString(R.string.X_TOOLS_API_NOT_RUNNING);
            default:
            case OTHER_ERROR:
                return getString(R.string.X_TOOLS_API_OTHER_ERROR);
        }
    }

    @Api
    public boolean isOk() {
        return ApiStatus.OK.equals(checkStatus());
    }

    public XContext getXContext() {
        return this.xContext;
    }

    public ApiMetaInfo[] getApiMetaInfo(String name) {
        List<ApiMetaInfo> retList = new ArrayList<>();
        if (this.apiMetaInfoList == null) {
            return new ApiMetaInfo[0];
        }
        for (ApiMetaInfo info : this.apiMetaInfoList) {
            if (info.getName().equals(name)) {
                retList.add(info);
            }
        }
        return retList.toArray(new ApiMetaInfo[retList.size()]);
    }

    public String[] getApiMetaInfoNames() {
        List<String> retList = new ArrayList<>();
        if (apiMetaInfoList == null) {
            return new String[0];
        }
        for (ApiMetaInfo info : apiMetaInfoList) {
            retList.add(info.getName());
        }
        return retList.toArray(new String[retList.size()]);
    }

    public ApiMetaInfo[] getApiMetaInfo() {
        if (apiMetaInfoList == null) {
            return new ApiMetaInfo[0];
        }
        return apiMetaInfoList.toArray(new ApiMetaInfo[apiMetaInfoList.size()]);
    }
}
