package x.tools.framework.script.lua;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import x.tools.framework.api.AbstractApi;
import x.tools.framework.api.ApiMetaInfo;
import x.tools.framework.api.ApiStatus;
import x.tools.framework.api.ParameterMetaInfo;
import x.tools.framework.error.AnnotationError;
import x.tools.framework.error.ScriptValueConvertError;
import x.tools.framework.error.XError;

public class LuaApiFunction extends VarArgFunction {
    private LuaScript luaScript;
    private AbstractApi api;
    private List<ApiMetaInfo> metaInfoList = new ArrayList<>();

    public LuaApiFunction(LuaScript luaScript, AbstractApi api) {
        this.luaScript = luaScript;
        this.api = api;
    }

    public void addMetaInfo(ApiMetaInfo metaInfo) {
        this.metaInfoList.add(metaInfo);
    }

    private ApiMetaInfo findMatch(Varargs args, AtomicReference<Object[]> retArgObjects) {
        List<Throwable> errors = new ArrayList<>();
        for (ApiMetaInfo metaInfo : this.metaInfoList) {
            ParameterMetaInfo[] parameterMetaData = metaInfo.getParameterMetaInfo();
            int count = parameterMetaData.length;
            boolean isVarArgs = count > 0 && parameterMetaData[count - 1].isVarArgs();
            int narg = args.narg();
            if (isVarArgs) {
                int argsCount = count - 1;
                if (narg >= argsCount) {
                    boolean isMatch = true;
                    int varArgsCount = narg - argsCount;
                    Object[] argObjects = new Object[count];
                    Object[] varArgObjects = new Object[varArgsCount];
                    argObjects[count] = varArgObjects;
                    for (int i = 1; i <= narg; i++) {
                        int ii = i - 1;
                        LuaObject arg = new LuaObject(args.arg(i));
                        if (i <= argsCount) {
                            try {
                                argObjects[ii] = arg.convertTo(parameterMetaData[ii].getType());
                                parameterMetaData[ii].checkAnnotation(argObjects[ii]);
                            } catch (ScriptValueConvertError | ClassCastException | AnnotationError e) {
                                errors.add(e);
                                isMatch = false;
                                break;
                            }
                        } else {
                            try {
                                varArgObjects[ii] = arg.convertTo(parameterMetaData[ii].getType().getComponentType());
                                parameterMetaData[ii].checkAnnotation(varArgObjects[ii]);
                            } catch (ScriptValueConvertError | ClassCastException | AnnotationError e) {
                                errors.add(e);
                                isMatch = false;
                                break;
                            }
                        }
                    }
                    if (isMatch) {
                        retArgObjects.set(argObjects);
                        return metaInfo;
                    }
                }
            } else {
                if (narg == count) {
                    boolean isMatch = true;
                    Object[] argObjects = new Object[count];
                    for (int i = 1; i <= count; i++) {
                        int ii = i - 1;
                        LuaObject arg = new LuaObject(args.arg(i));
                        try {
                            argObjects[ii] = arg.convertTo(parameterMetaData[ii].getType());
                            parameterMetaData[ii].checkAnnotation(argObjects[ii]);
                        } catch (ScriptValueConvertError | ClassCastException | AnnotationError e) {
                            errors.add(e);
                            isMatch = false;
                            break;
                        }
                    }
                    if (isMatch) {
                        retArgObjects.set(argObjects);
                        return metaInfo;
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(this.api.getNamespace()).append(".");
        sb.append(this.metaInfoList.get(0).getName()).append("\n");

        for (int i = 0; i < this.metaInfoList.size(); i++) {
            ApiMetaInfo metaInfo = this.metaInfoList.get(i);
            if (i > 0)
                sb.append(" or");
            sb.append(" require ");
            ParameterMetaInfo[] parameterMetaInfoArray = metaInfo.getParameterMetaInfo();
            for (int j = 0; j < parameterMetaInfoArray.length; j++) {
                ParameterMetaInfo parameterMetaInfo = parameterMetaInfoArray[j];
                sb.append(parameterMetaInfo.getName());
                sb.append(": ");
                sb.append(parameterMetaInfo.getType());
                if (j < parameterMetaInfoArray.length - 1)
                    sb.append(", ");
            }
            sb.append("\n");
        }
        sb.append("but got: ");
        for (int i = 1; i <= args.narg(); i++) {
            LuaValue value = args.arg(i);
            sb.append(value.tojstring());
            if (i <= args.narg() - 1)
                sb.append(", ");
        }
        sb.append("\n");
        if (errors.size() > 0) {
            sb.append("ERROR:\n");
            for (Throwable t : errors) {
                sb.append(t.toString()).append("\n");
            }
        }
        LuaValue.error(sb.toString());
        return null;
    }

    public static LuaValue error(Throwable t) { throw new LuaError(t); }

    @Override
    public Varargs invoke(Varargs args) {
        ApiStatus status = this.api.checkStatus();
        LuaValue.assert_(
                ApiStatus.OK.equals(status),
                String.format("Api %s status is not OK, status: %s", api.getNamespace(), status)
        );
        try {
            AtomicReference<Object[]> retArgObjects = new AtomicReference<>(null);
            ApiMetaInfo metaInfo = findMatch(args, retArgObjects);
            Object[] argObjects = retArgObjects.get();
            Object ret = metaInfo.getMethod().invoke(this.api, argObjects);
            return this.luaScript.createLuaValue(ret);
        } catch (Exception e) {
            error(e);
        }
        return LuaValue.NIL;
    }
}
