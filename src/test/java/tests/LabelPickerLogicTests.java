package tests;

import backend.resource.TurboIssue;
import backend.resource.TurboLabel;
import org.junit.Test;
import ui.components.pickers.LabelPickerUILogic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;

public class LabelPickerLogicTests {

    public LabelPickerUILogic prepareLogic() {
        TurboIssue issue = new TurboIssue("dummy/dummy", 1, "Issue 1");
        return prepareLogic(issue);
    }

    public LabelPickerUILogic prepareLogic(TurboIssue issue) {
        ArrayList<TurboLabel> repoLabels = new ArrayList<>();
        repoLabels.add(new TurboLabel("dummy/dummy", "Label 1"));
        repoLabels.add(new TurboLabel("dummy/dummy", "Label 2"));
        repoLabels.add(new TurboLabel("dummy/dummy", "Label 3"));
        repoLabels.add(new TurboLabel("dummy/dummy", "p.low"));
        repoLabels.add(new TurboLabel("dummy/dummy", "p.mid"));
        repoLabels.add(new TurboLabel("dummy/dummy", "p.high"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-aaa"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-bbb"));
        repoLabels.add(new TurboLabel("dummy/dummy", "f-ccc"));
        return new LabelPickerUILogic(issue, repoLabels);
    }

    public List<String> getLabels(LabelPickerUILogic logic) {
        return logic.getResultList().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    @Test
    public void groupTest() {
        // check for exclusive group toggling
        // we start with no labels
        LabelPickerUILogic logic = prepareLogic();
        assertEquals(0, getLabels(logic).size());
        // let's toggle two exclusive labels
        logic.toggleLabel("p.low");
        logic.toggleLabel("p.mid");
        // check to see that only one label has been applied
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, (boolean) logic.getResultList().get("p.mid"));
        // let's toggle two non-exclusive labels
        logic.toggleLabel("f-aaa");
        logic.toggleLabel("f-bbb");
        // so we should have 3 labels now
        assertEquals(3, getLabels(logic).size());

        // let's try starting with an issue with two exclusive labels in the same group
        TurboIssue issue = new TurboIssue("dummy/dummy", 1, "Issue 1");
        ArrayList<String> labels = new ArrayList<>();
        labels.add("p.low");
        labels.add("p.mid");
        issue.setLabels(labels);
        logic = prepareLogic(issue);
        // there should be two labels at first
        assertEquals(2, getLabels(logic).size());
        // we shall toggle one of the labels already in it
        logic.toggleLabel("p.mid");
        // we should be left with one label
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, (boolean) logic.getResultList().get("p.low"));

        // let's try starting with an issue with two exclusive labels in the same group
        issue = new TurboIssue("dummy/dummy", 1, "Issue 1");
        issue.setLabels(labels);
        logic = prepareLogic(issue);
        // there should be two labels at first
        assertEquals(2, getLabels(logic).size());
        // we shall toggle one of the labels in the same group but not in it
        logic.toggleLabel("p.high");
        // we should be left with one label
        assertEquals(1, getLabels(logic).size());
        assertEquals(true, (boolean) logic.getResultList().get("p.high"));
    }

}
