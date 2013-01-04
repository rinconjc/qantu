#!/bin/sh
OPTS="-Dfile.encoding=ISO-8859-1"
exec scala $0 $@
!#

import io.{Codec, Source}
import java.io.FileWriter
import java.text.DecimalFormat
import java.util.Calendar
import java.util.regex.{Matcher, Pattern}
import util.Random

/**
 * Simple HTML scrambler.
 * Replaces UPPERCASED strings (generally sensitive data is in UPPERCASE)  with random strings
 * Replaces 3 or more digit numbers with random numbers
 */
val textContent = "(?<=>|^)[^><$]+(?=<|$)".r.pattern
val upperCase = "(?<=\\W|^)[A-Z]+(?=\\W|$)".r.pattern
val decimals = "\\d+,\\d+".r.pattern
val numbers = "\\d{3,}".r.pattern
val stopwords = Set("AUD","CR","DR", "BSB")

def findAndReplace(line:String, pattern:Pattern, replaceWith: Matcher => String)={
  val sb = new StringBuffer()
  val m = pattern.matcher(line)
  while(m.find()){
    m.appendReplacement(sb, replaceWith(m))
  }
  m.appendTail(sb)
  sb.toString
}

val scrambled = args(0) + "-scrambled"
val out = new FileWriter(scrambled)
val df = new DecimalFormat("###,##0")
val thisYear = Calendar.getInstance().get(Calendar.YEAR)
Source.fromFile(args(0)).getLines().foreach{line=>
  val clean = findAndReplace(line, textContent, mm=>{
    val scrambledNumbers = findAndReplace(mm.group(), numbers, m=>{
      lazy val number = m.group().toInt
      if (m.group().length==4 && number>2000 && number <= thisYear) m.group() //possibly a year
      else Random.nextInt(math.pow(10, m.group().length).toInt).toString
    })
    val scrambledDecimals = findAndReplace(scrambledNumbers, decimals, m=>df.format(Random.nextDouble()*10000))
    findAndReplace(scrambledDecimals, upperCase, m=> if(stopwords.contains(m.group())) m.group()
    else Random.alphanumeric.take(m.group().length).mkString)
  })

  out.write(clean)
  out.write('\n')
}
out.close()
println("Scrambled filed created " + scrambled)

