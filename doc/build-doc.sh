#!/bin/sh

buildPdf()
{
pdflatex microMathematics$2.tex
pdflatex microMathematics$2.tex
mv microMathematics$2.pdf microMathematics-v$1$2.pdf
}

rm -f versionCode.sty && echo "\def\versionCode{$1}" >> versionCode.sty
rm -f *.pdf *.log *.aux
buildPdf $1
buildPdf $1 -ru
buildPdf $1 -de

rm -f *.log *.aux
ls -l *.pdf
