package org.unipi.matrixnorm

object Try2 extends App {
  def serialize(string: String) = string.getBytes("UTF-8")
  def deserialize(bytes: Array[Byte]) = new String(bytes, "UTF-8")

  def test(string: String) {
    val actual = deserialize(serialize(string))
    println(if (string == actual) "OK ".format(string)
    else "FAIL expected  got ".format(string, actual))
  }

  test("")
  test("foo")
  test("the quick brown fox jumps over the lazy dog")
  test("\u2192")
}