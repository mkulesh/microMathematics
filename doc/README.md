## Directory microMathematics/doc

This directory contains LaTeX source files for printable documentation (English, German, Russian, Brazilian Portuguese).

**Note:**
In order to build the documentation, the LaTeX (texlive) and following latex packages shall be installed on the host machine. For example, to install texlive on Fedora Workstation, perform following commands:

- as the root user:
```
# yum install texlive
# yum install texlive-lipsum texlive-sectsty texlive-t2 texlive-lastpage texlive-lettrine texlive-titling texlive-fonts-tlwg babel
# yum install texlive-cyrillic texlive-babel-russian texlive-hyphen-russian texlive-lh 
# yum install texlive-babel-german texlive-hyphen-german
# yum install texlive-babel-portuges texlive-hyphen-portuguese
# yum install texlive-collection-mathextra
# fmtutil --all
```
- as a local user (not root):
```
# fmtutil --missing
```

After LaTeX is installed, call 
```
# chmod +x build-doc.sh 
# ./build-doc.sh <version_code>
```