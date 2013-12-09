public interface ElementVisitor<T> {
    T visit(FirstElement firstElement);
    T visit(SecondElement secondElement);
}
