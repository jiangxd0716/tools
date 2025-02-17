package cn.gjing.tools.excel.driven;

import cn.gjing.tools.excel.ExcelFactory;
import cn.gjing.tools.excel.exception.ExcelResolverException;
import cn.gjing.tools.excel.read.resolver.ExcelBindReader;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Excel annotation-driven read handler
 *
 * @author Gjing
 **/
class ExcelDrivenReadHandler implements HandlerMethodReturnValueHandler {
    @Override
    public boolean supportsReturnType(MethodParameter methodParameter) {
        return methodParameter.hasMethodAnnotation(ExcelRead.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void handleReturnValue(Object o, @NonNull MethodParameter methodParameter, @NonNull ModelAndViewContainer modelAndViewContainer,
                                  @NonNull NativeWebRequest nativeWebRequest) throws Exception {
        if (o instanceof ExcelReadWrapper) {
            modelAndViewContainer.setRequestHandled(true);
            ExcelRead readAnno = methodParameter.getMethodAnnotation(ExcelRead.class);
            assert readAnno != null;
            ExcelReadWrapper wrapper = (ExcelReadWrapper) o;
            ExcelBindReader<?> reader = ExcelFactory.createReader(wrapper.getInputStream(), wrapper.getMapping(), wrapper.getIgnores() == null ? readAnno.ignores() : wrapper.getIgnores());
            reader.addListener(wrapper.getReadListeners())
                    .subscribe(wrapper.getResultReadListener())
                    .headBefore(readAnno.headBefore())
                    .check(readAnno.check())
                    .read(readAnno.headerIndex(), readAnno.value())
                    .finish();
            return;
        }
        throw new ExcelResolverException("Method return value type must be ExcelReadWrapper: " + methodParameter.getExecutable().getName());
    }
}
