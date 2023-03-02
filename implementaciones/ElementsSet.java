
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collection;

public class ElementsSet<E> extends HashSet<E>
{
    //Number of attempted elements insertions using the "add" method
    private int numberOfAddedElements = 0;

    public ElementsSet() {}

    @Override
    public boolean add(E element) {
        numberOfAddedElements++; //Counting the element added
        return super.add(element);
    } 

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        numberOfAddedElements += elements.size(); //Counting the elements added
        return super.addAll(elements);
    } 

    public int getNumberOfAddedElements() {
        return numberOfAddedElements;
    }

    // main
    public static void main(String[] args)
    {
        ElementsSet<String> set = new ElementsSet<String>();
        set.addAll(Arrays.asList("One", "Two", "Three"));
        System.out.println(set.getNumberOfAddedElements());
    }
}


