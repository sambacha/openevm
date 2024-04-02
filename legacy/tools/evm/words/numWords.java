package tech.pegasys.poc.witnesscodeanalysis.vm;

import tech.pegasys.poc.witnesscodeanalysis.vm.Address;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.bytes.MutableBytes32;

/** Static utility methods to work with VM words (that is, {@link Bytes32} values). */
public abstract class Words {
  private Words() {}

  /**
   * Creates a new word containing the provided address.
   *
   * @param address The address to convert to a word.
   * @return A VM word containing {@code address} (left-padded as according to the VM specification
   *     (Appendix H. of the Yellow paper)).
   */
  public static Bytes32 fromAddress(final Address address) {
    final MutableBytes32 bytes = MutableBytes32.create();
    address.copyTo(bytes, Bytes32.SIZE - Address.SIZE);
    return bytes;
  }

  /**
   * Extract an address from the the provided address.
   *
   * @param bytes The word to extract the address from.
   * @return An address build from the right-most 160-bits of the {@code bytes} (as according to the
   *     VM specification (Appendix H. of the Yellow paper)).
   */
  public static Address toAddress(final Bytes32 bytes) {
    return Address.wrap(bytes.slice(bytes.size() - Address.SIZE, Address.SIZE).copy());
  }

  /**
   * The number of words corresponding to the provided input.
   *
   * <p>In other words, this compute {@code input.size() / 32} but rounded up.
   *
   * @param input the input to check.
   * @return the number of (32 bytes) words that {@code input} spans.
   */
  public static int numWords(final Bytes input) {
    // m/n round up == (m + n - 1)/n: http://www.cs.nott.ac.uk/~psarb2/G51MPC/slides/NumberLogic.pdf
    return (input.size() + Bytes32.SIZE - 1) / Bytes32.SIZE;
  }
}
