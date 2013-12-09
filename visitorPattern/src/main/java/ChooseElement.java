public class ChooseElement {
    public String choose(Element element) {
        return element.accept(new ElementVisitor<String>() {
            @Override
            public String visit(FirstElement firstElement) {
                return "FirstElement";
            }

            @Override
            public String visit(SecondElement secondElement) {
                return "SecondElement";
            }
        });

    }
}
