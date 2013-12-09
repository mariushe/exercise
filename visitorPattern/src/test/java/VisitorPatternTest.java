import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class VisitorPatternTest {

    @Test
    public void testTest() throws Exception {

        ChooseElement chooseElement = new ChooseElement();

        String result = chooseElement.choose(new FirstElement());

        assertEquals(result, "FirstElement");

        result = chooseElement.choose(new SecondElement());

        assertEquals(result, "SecondElement");
    }
}
