/**
 * 
 */
package plugins.externalTestData;

import com.apitesting.core.testData.DynamicDataPreProcessor;
import io.cucumber.core.backend.StaticHookDefinition;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Clement Mukendi
 *
 */
@Slf4j
public class CustomGlue implements Glue {

  private static final ObjectFactoryServiceLoader serviceLoader
        = new ObjectFactoryServiceLoader(
            () -> Thread.currentThread().getContextClassLoader(), RuntimeOptions.defaultOptions()
  );
	private static final
  ObjectFactorySupplier objectFactorySupplier
      = new ThreadLocalObjectFactorySupplier(serviceLoader);
  public static void start() {
    try {
      log.info("=== stating feature external test data update ===");
      objectFactorySupplier.get().start();
      new DynamicDataPreProcessor().run();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public static void stop() {
    try {
      objectFactorySupplier.get().stop();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }
  @Override
  public void addBeforeAllHook(StaticHookDefinition staticHookDefinition) {}
  @Override
  public void addAfterAllHook(StaticHookDefinition staticHookDefinition) {}
  @Override
  public void addStepDefinition(StepDefinition stepDefinition) {}
  @Override
	public void addBeforeHook(HookDefinition beforeHook) {}
	@Override
	public void addAfterHook(HookDefinition afterHook) {}
	@Override
	public void addBeforeStepHook(HookDefinition beforeStepHook) {}
	@Override
	public void addAfterStepHook(HookDefinition afterStepHook) {}
	@Override
	public void addParameterType(ParameterTypeDefinition parameterTypeDefinition) {}
	@Override
	public void addDataTableType(DataTableTypeDefinition dataTableTypeDefinition) {}
	@Override
	public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {}
	@Override
	public void addDefaultDataTableEntryTransformer(
			DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
	}
	@Override
	public void addDefaultDataTableCellTransformer(
			DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {

	}
	@Override
	public void addDocStringType(DocStringTypeDefinition docStringTypeDefinition) {

	}

}
