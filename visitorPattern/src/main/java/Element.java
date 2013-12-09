public interface Element {
    public <T> T accept(ElementVisitor<T> visitor);
}
