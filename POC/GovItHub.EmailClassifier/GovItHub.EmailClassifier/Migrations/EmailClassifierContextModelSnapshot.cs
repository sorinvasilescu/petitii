using System;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Infrastructure;
using Microsoft.EntityFrameworkCore.Metadata;
using Microsoft.EntityFrameworkCore.Migrations;
using GovItHub.EmailClassifier.Models;

namespace GovItHub.EmailClassifier.Migrations
{
    [DbContext(typeof(EmailClassifierContext))]
    partial class EmailClassifierContextModelSnapshot : ModelSnapshot
    {
        protected override void BuildModel(ModelBuilder modelBuilder)
        {
            modelBuilder
                .HasAnnotation("ProductVersion", "1.0.0-rtm-21431");

            modelBuilder.Entity("GovItHub.EmailClassifier.Models.EmailClasses", b =>
                {
                    b.Property<int>("EmailClassId")
                        .ValueGeneratedOnAdd();

                    b.Property<string>("Name");

                    b.HasKey("EmailClassId");

                    b.ToTable("EmailClasses");
                });
        }
    }
}
