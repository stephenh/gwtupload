package jsupload.client;

import gwtupload.client.HasJsData;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.ExportClosure;
import org.timepedia.exporter.client.ExportPackage;
import org.timepedia.exporter.client.Exportable;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

@Export
@ExportPackage("jsu")
@ExportClosure
public interface JsChangeClosure extends Exportable {
	@Export
	public void onChange(Object object);

}

/**
 * @author Manolo Carrasco Moñino
 * 
 * An utility class for storing static methods
 *
 */
class JsUtils {

	/**
	 * Creates a valueChangeHandler that executes a ChangeClosure
	 * 
	 * @param <T>
	 * @param clazz
	 * @param jsChange
	 * @return
	 */
	public final static <T extends HasJsData> ValueChangeHandler<T> getClosureHandler(final T clazz, final JsChangeClosure jsChange) {
		return new ValueChangeHandler<T>() {
			public void onValueChange(ValueChangeEvent<T> event) {
				Object data = null;
				if (jsChange != null) {
					if (event != null && event.getValue() != null)
						data = event.getValue().getData();
					jsChange.onChange(data);
				}
			}
		};
	}
}
